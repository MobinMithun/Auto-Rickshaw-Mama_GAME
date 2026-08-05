import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:desi_turbo_rush/main.dart';

void main() {
  testWidgets('App launches smoke test', (WidgetTester tester) async {
    await tester.pumpWidget(const DesiTurboRushApp());
    expect(find.byType(MaterialApp), findsOneWidget);
  });
}
