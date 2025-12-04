package uk.gov.justice.digital.hmpps.healthandmedication.jpa.repository.utils

import org.springframework.test.context.jdbc.Sql

@Sql("classpath:jpa/repository/reset.sql")
@Sql("classpath:resource/healthandmedication/health.sql")
@Sql("classpath:resource/healthandmedication/food_allergies.sql")
@Sql("classpath:resource/healthandmedication/medical_dietary_requirements.sql")
@Sql("classpath:resource/healthandmedication/personalised_dietary_requirements.sql")
@Sql("classpath:resource/healthandmedication/prisoner_location.sql")
@Sql("classpath:resource/healthandmedication/field_metadata.sql")
@Sql("classpath:resource/healthandmedication/field_history.sql")
annotation class RepopulateDb
