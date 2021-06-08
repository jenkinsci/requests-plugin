
## Version History

Version 2.2.11 (June 08, 2021)

-       Security fix: Bumped httpclient from 4.3.6 to 4.5.13.
-       Security fix: Bumped httpcore from 4.4.3 to 4.4.13.


Version 2.2.10 (June 01, 2021)

-       Fix issue with spaces in names

Version 2.2.9 (November 16, 2020)

-       Fix issue with Folders related to encoding from previous release

Version 2.2.8 (November 04, 2020)

- 		Changes to address issues with SECURITY-2136
-		Fix project names that require encoding for POST urls

Version 2.2.7 (October 07, 2020)

-       Fix Post error on Process Requests that started with Jenkins 2.249.1 for CSRF changes
-       Fix for test email address
-       Dependency updates

Version 2.2.6 (February 12, 2020)

-	Add New Delete Folder request [JIRA] (JENKINS-61011)
-	Add New Rename Folder request [JIRA] (JENKINS-61011)

Version 2.2.5 (January 24, 2020)

-	Add multiple email support to test email configuration.

Version 2.2.4 (January 16, 2020)

-	JENKINS-60780 Include change for Delete Build, Rename Job, Unlock Build along with Delete Job.
-	Add Email support to Rename requests.
-	Remove multiple emails on the same request.

Version 2.2.3 (January 16, 2020)

-	JENKINS-60780 Additional change for this issue.

Version 2.2.2 (January 15, 2020)

-	JENKINS-60780 Fixed delete job when within multiple folders views.
-	JENKINS-60781 Allow multiple admin email addresses.

Version 2.2.1 (January 08, 2020)

-	Changed from user password for Delete/Unlock requests to a jenkins user token.
-	Delete build will now unlock before deleting if its locked instead of warning of locked build.

Version 2.0.5 (May 30, 2019)

-   Added Rename Job support back

Version 2.0.4 (May 29, 2019)

-   Added support for Pipeline jobs and builds

Version 2.0.3 (May 28, 2019)

-   Fixed the Creation date format in the Pending request page

Version 2.0.2 (May 24, 2019)

-   Initial release

