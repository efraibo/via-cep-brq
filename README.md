# via-cep-brq

Este projeto é um exemplo de aplicativo de busca de CEP (Código de Endereçamento Postal) utilizando a API Via CEP, desenvolvido como parte do processo de entrevista na empresa BRQ.

## Instruções de instalação sem o uso do Docker

1. Baixe o projeto: `git clone https://github.com/efraibo/via-cep-brq.git`
2. Abra o terminal e navegue até a pasta do projeto.
3. Execute o seguinte comando no terminal: `mvn clean package`
4. Inicialize a aplicação com o comando: `java -jar target/via-cep-brq-0.0.1.jar`
5. Abra o Swagger no navegador através do link: `http://localhost:8080/swagger-ui`

## Instruções de instalação utilizando o Docker

1. Baixe o projeto: `git clone https://github.com/efraibo/via-cep-brq.git`
2. Abra o terminal e navegue até a pasta do projeto.
3. Execute o comando: `docker-compose up`
4. Abra o Swagger no navegador através do link: `http://localhost:8080/swagger-ui/index.html`
   - User: Admin
   - Password: via-cep-brq

Observações:
- Se a aplicação for executada utilizando o Docker, será possível visualizar o dashboard do SonarQube, que está disponível no link: `http://localhost:9000/`. Todas as questões (issues) foram resolvidas e também está disponível o coverage.
- Testes unitários foram adicionados para garantir a qualidade e padronização do código, utilizando a biblioteca `https://www.archunit.org/`.

**Recursos adicionais:**
Um recurso extra implementado neste projeto é o fluxo de retry. Caso ocorra algum erro durante a chamada às APIs de busca de CEP, o sistema automaticamente tentará realizar novas chamadas de forma automática, permitindo uma maior robustez e tolerância a falhas na aplicação.

Essas instruções são fornecidas para auxiliar na configuração e execução do projeto. Caso ocorram dúvidas ou problemas, não hesite em entrar em contato com o desenvolvedor.