package com.bookstore.integration;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Integration Test Suite that runs all integration tests
 * 
 * This suite includes:
 * - Base integration tests for all REST endpoints
 * - Contract tests for FeignClient interfaces
 * - End-to-end workflow tests
 * - Performance integration tests
 * - Comprehensive integration test suite
 * 
 * To run only this suite:
 * mvn test -Dtest=IntegrationTestSuite
 */
@Suite
@SuiteDisplayName("Bookstore Integration Test Suite")
@SelectPackages("com.bookstore.integration")
@IncludeClassNamePatterns({
    ".*IntegrationTest.*",
    ".*ContractTest.*",
    ".*WorkflowTest.*",
    ".*TestSuite.*"
})
public class IntegrationTestSuite {
    // This class serves as a test suite runner
    // All test configuration is done via annotations
}