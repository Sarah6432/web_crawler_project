Web Crawler TISS - ANS 🩺🤖
Este projeto é um Web Crawler desenvolvido em Groovy utilizando a biblioteca Jsoup. O objetivo é automatizar a coleta, download e estruturação de dados do portal da Agência Nacional de Saúde Suplementar (ANS), focando no Padrão TISS (Troca de Informação de Saúde Suplementar).

🚀 Funcionalidades
O bot executa um fluxo completo de automação dividido em três etapas principais:

Downloads de Componentes (Janeiro/2026): * Navega dinamicamente pelo portal até a página da competência mais recente.

Localiza e baixa o Componente de Comunicação.

Organiza os arquivos em uma estrutura de pastas inteligente (downloads/nome_do_componente/).

Utiliza Multithreading (ExecutorService) para otimizar a performance dos downloads.

Extração de Histórico de Versões:

Acessa a página de histórico de todas as versões do padrão TISS.

Coleta dados de Competência, Publicação e Início de Vigência.

Filtra os registros retroativos até Janeiro/2016.

Gera automaticamente um arquivo historico_tiss.csv estruturado para análise em Excel.

Coleta de Tabelas Relacionadas:

Navega até a seção de tabelas auxiliares.

Realiza o download da Tabela de Erros no envio para a ANS diretamente na pasta de downloads.

🛠️ Tecnologias Utilizadas
Linguagem: Groovy (JVM)

Parsing HTML: Jsoup

Gerenciamento de Dependências: Gradle

Concorrência: Java Concurrent (Fixed Thread Pool)

📦 Estrutura de Arquivos Gerada
Após a execução, o projeto organiza os dados da seguinte forma:

Plaintext
.
├── downloads/
│   ├── componente_de_comunicacao/
│   │   └── componente.zip
│   └── tabela de erros.xlsx (ou .zip/.pdf)
├── historico_tiss.csv
└── ...
⚙️ Como Executar
Certifique-se de ter o JDK 11+ e o Groovy instalados.

Clone o repositório:

Bash
git clone https://github.com/Sarah6432/web_crawler_project.git
Importe o projeto em sua IDE (IntelliJ IDEA recomendada) como um projeto Gradle.

Execute a classe BotCrawler.groovy.

🛡️ Boas Práticas Implementadas
User-Agent: Identificação da requisição para evitar bloqueios simples de segurança.

Sanitização de Nomes: Remoção de caracteres especiais e espaços para criação de pastas seguras no Sistema Operacional.

Timeouts: Gerenciamento de tempo de conexão e leitura para lidar com arquivos pesados e instabilidade do servidor.

Tratamento de Erros: Blocos try-catch para garantir que falhas em um download não interrompam o fluxo de extração de dados.

Desenvolvido por Sarah Silva Lima 🚀
