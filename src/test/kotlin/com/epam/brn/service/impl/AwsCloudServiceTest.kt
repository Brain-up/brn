package com.epam.brn.service.impl

import com.epam.brn.config.AwsConfig
import java.time.Instant
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class AwsCloudServiceTest {
    @InjectMocks
    lateinit var awsCloudService: AwsCloudService
    @Mock
    lateinit var awsConfig: AwsConfig

    @Test
    fun `should get correct signature for client upload`() {
        // GIVEN
        Mockito.`when`(awsConfig.instant()).thenReturn(Instant.ofEpochMilli(1580384357114).atOffset(ZoneOffset.UTC))
        Mockito.`when`(awsConfig.uuid()).thenReturn("c49791b2-b27b-4edf-bac8-8734164c20e6")
        Mockito.`when`(awsConfig.xamzCredential).thenReturn("%s/%s/us-east-2/s3/aws4_request")
        Mockito.`when`(awsConfig.expireAfterDuration).thenReturn("PT10H")
        Mockito.`when`(awsConfig.accessRuleCanned).thenReturn("Private")
        Mockito.`when`(awsConfig.accessKeyId).thenReturn("AKIAI7KLKATWVCMEKGPA")
        Mockito.`when`(awsConfig.uploadKeyStartsWith).thenReturn("tasks/")
        Mockito.`when`(awsConfig.bucketName).thenReturn("somebucket")
        Mockito.`when`(awsConfig.secretAccessKey).thenReturn("99999999999999999999999999999")
        Mockito.`when`(awsConfig.region).thenReturn("us-east-2")
        Mockito.`when`(awsConfig.serviceName).thenReturn("s3")
        Mockito.`when`(awsConfig.bucketLink).thenReturn("http://somebucket.s3.amazonaws.com")
        Mockito.`when`(awsConfig.successActionRedirect).thenReturn("")
        Mockito.`when`(awsConfig.contentTypeStartsWith).thenReturn("")
        Mockito.`when`(awsConfig.metaTagStartsWith).thenReturn("")
        Mockito.`when`(awsConfig.accessRule()).thenCallRealMethod()
        Mockito.`when`(awsConfig.expireAfter()).thenCallRealMethod()
        Mockito.`when`(awsConfig.getConditions()).thenCallRealMethod()
        // WHEN
        val signature = awsCloudService.signatureForClientDirectUpload("")
        // THEN
        val signatureExpected: Map<String, Any> = mapOf(
            "action" to "http://somebucket.s3.amazonaws.com",
            "input" to listOf(
                mapOf("policy" to "ew0KICAiY29uZGl0aW9ucyIgOiBbIHsNCiAgICAiYnVja2V0IiA6ICJzb21lYnVja2V0Ig0KICB9LCB7DQogICAgImFjbCIgOiAicHJpdmF0ZSINCiAgfSwgWyAic3RhcnRzLXdpdGgiLCAiJGtleSIsICJ0YXNrcy8iIF0sIHsNCiAgICAieC1hbXotbWV0YS11dWlkIiA6ICJjNDk3OTFiMi1iMjdiLTRlZGYtYmFjOC04NzM0MTY0YzIwZTYiDQogIH0sIHsNCiAgICAieC1hbXotc2VydmVyLXNpZGUtZW5jcnlwdGlvbiIgOiAiQUVTMjU2Ig0KICB9LCB7DQogICAgIngtYW16LWNyZWRlbnRpYWwiIDogIkFLSUFJN0tMS0FUV1ZDTUVLR1BBLzIwMjAwMTMwL3VzLWVhc3QtMi9zMy9hd3M0X3JlcXVlc3QiDQogIH0sIHsNCiAgICAieC1hbXotYWxnb3JpdGhtIiA6ICJBV1M0LUhNQUMtU0hBMjU2Ig0KICB9LCB7DQogICAgIngtYW16LWRhdGUiIDogIjIwMjAwMTMwVDExMzkxN1oiDQogIH0gXSwNCiAgImV4cGlyYXRpb24iIDogIjIwMjAtMDEtMzBUMjE6Mzk6MTcuMTE0WiINCn0="),
                mapOf("x-amz-signature" to "5ed1002da60d3ad165667e04a95f7b1d75d13438fddab0d3a87f173d5a7fb4fc"),
                mapOf("key" to "tasks/\${filename}"),
                mapOf("acl" to "private"),
                mapOf("x-amz-meta-uuid" to "c49791b2-b27b-4edf-bac8-8734164c20e6"),
                mapOf("x-amz-server-side-encryption" to "AES256"),
                mapOf("x-amz-credential" to "AKIAI7KLKATWVCMEKGPA/20200130/us-east-2/s3/aws4_request"),
                mapOf("x-amz-algorithm" to "AWS4-HMAC-SHA256"),
                mapOf("x-amz-date" to "20200130T113917Z")
            )
        )
        Assertions.assertEquals(signatureExpected, signature)
    }
}
