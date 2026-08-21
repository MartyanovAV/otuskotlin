FROM liquibase/liquibase:4.31.1

ENV LIQUIBASE_COMMAND_CHANGELOG_FILE=changelog-master.yaml

COPY . /liquibase/project/

WORKDIR /liquibase/project

CMD ["update"]
