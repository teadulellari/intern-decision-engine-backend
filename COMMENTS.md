## Comments about TICKET-102

This Branch is based on `TICKET101-review` which contains the changes I did for TASK101. I took this approach because I believe it would be easier to see changes specific to TICKET102

`isEligibleAgeForLoan` method is inside of `CountryService` because checking the legality of the loan should not be responsibility of `DecisionEngine` as every country has its own laws.
I believe putting this into `CountryService` makes more sense and decouples the necessary country implementation from `DecisionEngine`

I also created special exception class for age control but if these exceptions increases, we might generalize it as `DecisionEngineException` and set some variables / flags to identify the reasons of it

in TICKET102, there isn't any changes on frontend side as the structure the intern defined handles the exceptions through the `errorMessage` field of the response, therefore we can display the age related rejection info to the user without any special modification.

If this task would not include limitations and assumptions, I would suggest us to implement a `CountryService` class and create `CountryResolver` interface that will contain common country related operations such as `getLegalAgeForLoan` and we would implement this interface for each Baltic country.
During the usage, the `DecisionEngine` would send the personal code to `CountryService` and this service parse the ID/personal code and match it with correct implementation of `CountryResolver` and call the methods of that implementation.
We also could use the `java-personal-code` dependency as it contains other country validators and parsers but some of them are outdated (for example it only supports legacy Latvian personal code) so we might need to implement our own parser/validator as well.

The solution assumes that the `DecisionEngineConstant.MAXIMUM_LOAN_PERIOD` constant will be based on `month` values. As the parser returns a `Period` object, I need to normalize the months instead of just calling `.plusMonths()` method.
Because `Period` object is not representing a date, and therefore it does not handle month value gets higher than 12.

One of the potential issues with this task would be the test cases. As the test cases depends on the concept of age, every year the static personal codes would be in risk as they get older, the tests has a chance behave differently.
In order to avoid this problem, instead of having static personalCode, I am generating the personalCodes automatically in order to avoid this problem in the future. 