package uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlMergeMode
import org.springframework.test.context.jdbc.SqlMergeMode.MergeMode.MERGE
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.healthandmedication.integration.TestBase

@DataJpaTest
@AutoConfigureJson
@Transactional
@AutoConfigureTestDatabase(replace = NONE)
@SqlMergeMode(MERGE)
@Sql("classpath:jpa/repository/reset.sql")
abstract class RepositoryTest : TestBase()
