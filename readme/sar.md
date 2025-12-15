[< Back](../README.md)
---

## Subject Access Requests

This service supports the HMPPS Subject Access Request (SAR) framework and implements the relevant SAR endpoints.  
Please refer to the SAR documentation in Confluence for more details.

We use the [SAR test library](https://github.com/ministryofjustice/hmpps-subject-access-request-lib) to alert us of any
changes to the SAR specification. This means that any changes to the database schema may break the tests in
`SubjectAccessRequestIntegrationTest`. When this happens, please refer to the SAR documentation in Confluence for
the process of updating the SAR template. Release to production must not happen until the change is approved unless in
exceptional circumstances.

In order to regenerate the reference SAR test files used in the tests, run the following command:

```
SAR_GENERATE_ACTUAL=true ./gradlew test --tests "uk.gov.justice.digital.hmpps.healthandmedication.sar.SubjectAccessRequestIntegrationTest"
```

This will generate the following files:

- `src/test/resources/entity-schema.json.log`
- `src/test/resources/sar-api-response.json.log`
- `src/test/resources/sar-generated-report.html.log`

These can be used to update the reference files:

- `src/test/resources/entity-schema.json`
- `src/test/resources/sar-api-response.json`
- `src/test/resources/sar-generated-report.html`
