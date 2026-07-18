# Notes for US-000008

## Technical Decisions
- The demo application obtains the predefined provider list from `DefaultProviders` and binds it to the component's entries property.
- The warning label lives in the demo UI, not in the reusable component.
- Warning visibility is bound to the selected provider's `googleLogin` property.

## Trade-offs
- The warning text is fixed for now; localization is out of scope.
- The component remains agnostic of the warning feature, keeping it reusable.
