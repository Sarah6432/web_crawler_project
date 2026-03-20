package org.example

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class BotCrawler {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    private static final int TIMEOUT_MS = 15000

    static void main(String[] args) {
        String urlBase = "https://www.gov.br/ans/pt-br"

        Document home = conectar(urlBase)
        if (!home) return

        String urlPrestador = buscarUrlPrestador(home)
        Document pgPrestador = conectar(urlPrestador)
        if (!pgPrestador) return

        String urlTiss = buscarUrlTiss(pgPrestador)
        Document pgTissPrincipal = conectar(urlTiss)
        if (!pgTissPrincipal) return

        String urlJan2026 = "https://www.gov.br/ans/pt-br/assuntos/prestadores/padrao-para-troca-de-informacao-de-saude-suplementar-2013-tiss/padrao-tiss-janeiro-2026"
        Document pgDownload = conectar(urlJan2026)
        if (pgDownload) {
            extrairEDownloadParalelo(pgDownload)
        }

        String urlHistorico = "https://www.gov.br/ans/pt-br/assuntos/prestadores/padrao-para-troca-de-informacao-de-saude-suplementar-2013-tiss/padrao-tiss-historico-das-versoes-dos-componentes-do-padrao-tiss"
        Document pgHistorico = conectar(urlHistorico)
        if (pgHistorico) {
            gerarCsvHistorico(pgHistorico)
        }

        String urlTabelasRelacionadas = "https://www.gov.br/ans/pt-br/assuntos/prestadores/padrao-para-troca-de-informacao-de-saude-suplementar-2013-tiss/padrao-tiss-tabelas-relacionadas"
        Document pgTabelas = conectar(urlTabelasRelacionadas)
        if (pgTabelas) {
            baixarTabelaErros(pgTabelas)
        }
    }

    private static Document conectar(String url) {
        try {
            println "Conectando: $url"
            return Jsoup.connect(url).userAgent(USER_AGENT).timeout(TIMEOUT_MS).get()
        } catch (Exception e) {
            println "Erro na conexão: $url -> ${e.message}"
            return null
        }
    }

    private static String buscarUrlPrestador(Document docHome) {
        def link = docHome.select("a[href=https://www.gov.br/ans/pt-br/assuntos/prestadores]").first()
        if (!link) link = docHome.select("a:contains(Prestador)").first()
        return link ? link.attr("abs:href") : null
    }

    private static String buscarUrlTiss(Document docPrestadores) {
        def link = docPrestadores.select("a:contains(TISS)").first()
        return link ? link.attr("abs:href") : "https://www.gov.br/ans/pt-br/assuntos/prestadores/padrao-para-troca-de-informacao-de-saude-suplementar-2013-tiss"
    }

    private static void baixarTabelaErros(Document docTabelas) {
        println "\nLocalizando a tabela de erros..."
        Element linkTabela = docTabelas.select("a:contains(Tabela de erros no envio para a ANS)").first()

        if (linkTabela) {
            String urlDownload = linkTabela.attr("abs:href")
            File dirDownloads = new File("downloads")
            if (!dirDownloads.exists()) dirDownloads.mkdir()

            String extensao = urlDownload.contains(".") ? urlDownload.substring(urlDownload.lastIndexOf(".")) : ".xlsx"
            if (extensao.contains("?")) extensao = extensao.substring(0, extensao.indexOf("?"))

            File arquivoLocal = new File(dirDownloads, "tabela de erros" + extensao)
            println "Baixando tabela de erros para: ${arquivoLocal.absolutePath}"

            try {
                URLConnection conn = new URL(urlDownload).openConnection()
                conn.setConnectTimeout(TIMEOUT_MS)
                conn.setRequestProperty("User-Agent", USER_AGENT)
                conn.getInputStream().withStream { input ->
                    arquivoLocal.withOutputStream { output ->
                        output << input
                    }
                }
                println "Tabela de erros baixada com sucesso!"
            } catch (Exception e) {
                println "Erro ao baixar tabela de erros: ${e.message}"
            }
        }
    }

    private static void gerarCsvHistorico(Document docHistorico) {
        println "\nGerando csv do histórico:"
        StringBuilder csvContent = new StringBuilder()
        csvContent.append("Competencia;Publicacao;Inicio de Vigencia\n")

        def linhas = docHistorico.select("#content-core table tbody tr")
        for (Element linha : linhas) {
            def colunas = linha.select("td")
            if (colunas.size() >= 3) {
                String comp = colunas[0].text().trim()
                String pub = colunas[1].text().trim()
                String vig = colunas[2].text().trim()
                csvContent.append("$comp;$pub;$vig\n")
                if (comp.equalsIgnoreCase("jan/2016")) break
            }
        }
        new File("historico_tiss.csv").write(csvContent.toString(), "UTF-8")
        println "Arquivo 'historico_tiss.csv' gerado!"
    }

    private static void extrairEDownloadParalelo(Document doc) {
        def executor = Executors.newFixedThreadPool(1)
        println "\nLocalizando link de Comunicacao..."

        Element linhaComunicacao = doc.select("tr:contains(Comunicação)").first()
        if (linhaComunicacao) {
            Element linkNode = linhaComunicacao.select("a[href\$=.zip]").first()
            if (linkNode) {
                String urlDl = linkNode.attr("abs:href")
                executor.submit({
                    baixar("Componente de Comunicacao", urlDl)
                } as Runnable)
            }
        }

        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.MINUTES)
    }

    private static void baixar(String nomeOriginal, String url) {
        String nomePasta = sanitizar(nomeOriginal)
        File dirDownloads = new File("downloads")
        if (!dirDownloads.exists()) dirDownloads.mkdir()

        File pastaFinal = new File(dirDownloads, nomePasta)
        if (!pastaFinal.exists()) pastaFinal.mkdir()

        File arquivoLocal = new File(pastaFinal, "componente.zip")
        println "Baixando agora para: ${arquivoLocal.absolutePath}"

        try {
            URLConnection conn = new URL(url).openConnection()
            conn.setConnectTimeout(TIMEOUT_MS)
            conn.setReadTimeout(120000)
            conn.setRequestProperty("User-Agent", USER_AGENT)

            conn.getInputStream().withStream { input ->
                arquivoLocal.withOutputStream { output ->
                    output << input
                }
            }
            println "Download concluído com sucesso!"
        } catch (Exception e) {
            println "Erro fatal no download: ${e.message}"
        }
    }

    private static String sanitizar(String nome) {
        return nome.toLowerCase()
                .replaceAll(/[áàâã]/, 'a').replaceAll(/[éèê]/, 'e')
                .replaceAll(/[íìî]/, 'i').replaceAll(/[óòôõ]/, 'o')
                .replaceAll(/[úùû]/, 'u').replaceAll(/ç/, 'c')
                .replaceAll(/[^a-z0-9]/, '_').replaceAll(/_{2,}/, '_')
                .replaceAll(/^_|_$/, '')
    }
}