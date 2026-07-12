import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:nihongo_mochi_flutter/widgets/recap_components.dart';

void main() {
  testWidgets('PaginationControls renders correctly and handles clicks', (WidgetTester tester) async {
    bool prevClicked = false;
    bool nextClicked = false;

    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: PaginationControls(
            currentPage: 1,
            totalPages: 3,
            onPrevClick: () => prevClicked = true,
            onNextClick: () => nextClicked = true,
          ),
        ),
      ),
    );

    // Verify it displays the page text
    expect(find.text('Page 2 of 3'), findsOneWidget);

    // Verify the buttons are present
    final prevFinder = find.byIcon(Icons.arrow_back);
    final nextFinder = find.byIcon(Icons.arrow_forward);
    expect(prevFinder, findsOneWidget);
    expect(nextFinder, findsOneWidget);

    // Click previous
    await tester.tap(prevFinder);
    await tester.pump();
    expect(prevClicked, isTrue);

    // Click next
    await tester.tap(nextFinder);
    await tester.pump();
    expect(nextClicked, isTrue);
  });
}
