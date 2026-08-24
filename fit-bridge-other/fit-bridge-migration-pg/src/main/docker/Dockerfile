FROM liquibase/liquibase:4.33.0

ENV LIQUIBASE_COMMAND_CHANGELOG_FILE=changelog-master.yaml

COPY . /liquibase/project/

WORKDIR /liquibase/project

CMD ["update"]
