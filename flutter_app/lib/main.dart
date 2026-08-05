import 'package:flutter/material.dart';
import 'ui/main_game_container.dart';

void main() {
  runApp(const DesiTurboRushApp());
}

class DesiTurboRushApp extends StatelessWidget {
  const DesiTurboRushApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Desi Turbo Rush',
      debugShowCheckedModeBanner: false,
      theme: ThemeData.from(
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFFFFB03B),
          secondary: Color(0xFF2FA15A),
          surface: Color(0xFF1C1A2E),
        ),
        useMaterial3: true,
      ).copyWith(
        scaffoldBackgroundColor: const Color(0xFF0D0B1F),
      ),
      home: const MainGameContainer(),
    );
  }
}
