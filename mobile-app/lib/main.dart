import 'dart:async';

import 'package:flutter/material.dart';

void main() => runApp(const CyberSurakshaApp());

class SG {
  static const background = Color(0xFF0B0D10);
  static const surface = Color(0xFF121519);
  static const raised = Color(0xFF191D22);
  static const border = Color(0xFF292F36);
  static const text = Color(0xFFF4F6F8);
  static const secondary = Color(0xFFA8B0BA);
  static const muted = Color(0xFF737E89);
  static const accent = Color(0xFF36C99A);
  static const danger = Color(0xFFF05252);
  static const warning = Color(0xFFF2B84B);
  static const warningTint = Color(0xFF382B16);
}

class CyberSurakshaApp extends StatelessWidget {
  const CyberSurakshaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'CyberSuraksha',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        scaffoldBackgroundColor: SG.background,
        colorScheme: const ColorScheme.dark(
          primary: SG.accent,
          secondary: SG.accent,
          surface: SG.surface,
          error: SG.danger,
          onPrimary: SG.background,
          onSurface: SG.text,
        ),
        appBarTheme: const AppBarTheme(
          backgroundColor: SG.background,
          foregroundColor: SG.text,
          elevation: 0,
        ),
        cardTheme: CardThemeData(
          color: SG.surface,
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(14),
            side: const BorderSide(color: SG.border),
          ),
        ),
        textTheme: const TextTheme(
          headlineSmall: TextStyle(color: SG.text, fontWeight: FontWeight.w700),
          titleLarge: TextStyle(color: SG.text, fontWeight: FontWeight.w600),
          titleMedium: TextStyle(color: SG.text, fontWeight: FontWeight.w600),
          bodyLarge: TextStyle(color: SG.text),
          bodyMedium: TextStyle(color: SG.secondary),
          bodySmall: TextStyle(color: SG.muted),
        ),
      ),
      home: const PersonTwoSafetyPage(),
    );
  }
}

class PersonTwoSafetyPage extends StatefulWidget {
  const PersonTwoSafetyPage({super.key});

  @override
  State<PersonTwoSafetyPage> createState() => _PersonTwoSafetyPageState();
}

class _PersonTwoSafetyPageState extends State<PersonTwoSafetyPage> {
  bool _online = true;
  bool _journeyActive = false;
  bool _checkInActive = false;
  int _journeySeconds = 3600;
  int _checkInSeconds = 900;
  Timer? _journeyTimer;
  Timer? _checkInTimer;

  final List<String> _pendingAlerts = ['SOS alert waiting to send'];

  final List<_SafePoint> _safePoints = const [
    _SafePoint('Central Police Station', 'Police station', '450 m', Icons.local_police_outlined),
    _SafePoint('City General Hospital', 'Emergency department', '800 m', Icons.local_hospital_outlined),
    _SafePoint('Campus Security Desk', 'Campus security', '1.2 km', Icons.shield_outlined),
  ];

  final List<_IncidentEvent> _events = const [
    _IncidentEvent('10:42 AM', 'Location updated', 'Latest position recorded'),
    _IncidentEvent('10:40 AM', 'Trusted contact notified', 'Alert delivery recorded'),
    _IncidentEvent('10:38 AM', 'Safety incident created', 'Manual SOS activation'),
  ];

  String _formatTime(int seconds) {
    final minutes = (seconds ~/ 60).toString().padLeft(2, '0');
    final remaining = (seconds % 60).toString().padLeft(2, '0');
    return '$minutes:$remaining';
  }

  void _showMessage(String message) {
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(SnackBar(content: Text(message)));
  }

  void _startJourney() {
    setState(() {
      _journeyActive = true;
      _journeySeconds = 3600;
    });

    _journeyTimer?.cancel();
    _journeyTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_journeySeconds <= 1) {
        timer.cancel();
        setState(() {
          _journeySeconds = 0;
          _journeyActive = false;
        });
        _showMessage('Demo journey link expired.');
        return;
      }
      setState(() => _journeySeconds--);
    });

    _showMessage('Demo journey link created for your trusted contact.');
  }

  void _stopJourney() {
    _journeyTimer?.cancel();
    setState(() => _journeyActive = false);
    _showMessage('Demo journey link revoked.');
  }

  void _startCheckIn() {
    setState(() {
      _checkInActive = true;
      _checkInSeconds = 900;
    });

    _checkInTimer?.cancel();
    _checkInTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_checkInSeconds <= 1) {
        timer.cancel();
        setState(() {
          _checkInSeconds = 0;
          _checkInActive = false;
        });
        _showMessage('Demo timer ended. Connect this to emergency escalation.');
        return;
      }
      setState(() => _checkInSeconds--);
    });
  }

  void _confirmSafe() {
    _checkInTimer?.cancel();
    setState(() {
      _checkInActive = false;
      _checkInSeconds = 900;
    });
    _showMessage('Check-in confirmed in demo mode.');
  }

  void _retryAlert(int index) {
    if (!_online) {
      _showMessage('Offline: alert remains queued on this device.');
      return;
    }

    setState(() => _pendingAlerts.removeAt(index));
    _showMessage('Demo alert marked as sent.');
  }

  @override
  void dispose() {
    _journeyTimer?.cancel();
    _checkInTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Row(
          children: [
            Icon(Icons.shield_moon_outlined, color: SG.accent),
            SizedBox(width: 10),
            Text('CyberSuraksha', style: TextStyle(fontWeight: FontWeight.w700)),
          ],
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 8),
            child: TextButton.icon(
              onPressed: () => setState(() => _online = !_online),
              icon: Icon(
                Icons.circle,
                size: 9,
                color: _online ? SG.accent : SG.warning,
              ),
              label: Text(
                _online ? 'Online' : 'Offline',
                style: TextStyle(color: _online ? SG.accent : SG.warning),
              ),
            ),
          ),
        ],
      ),
      body: SafeArea(
        child: LayoutBuilder(
          builder: (context, constraints) {
            final wide = constraints.maxWidth >= 850;

            return Center(
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 1180),
                child: ListView(
                  padding: const EdgeInsets.fromLTRB(20, 20, 20, 32),
                  children: [
                    const Text(
                      'SAFETY UTILITIES',
                      style: TextStyle(
                        color: SG.accent,
                        letterSpacing: 1.5,
                        fontSize: 11,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      'Your safety, in one place',
                      style: Theme.of(context).textTheme.headlineSmall,
                    ),
                    const SizedBox(height: 6),
                    const Text(
                      'Location, check-ins, and incident updates.',
                      style: TextStyle(color: SG.secondary),
                    ),
                    const SizedBox(height: 22),
                    if (!_online) ...[
                      const _StatusBanner(
                        icon: Icons.wifi_off,
                        color: SG.warning,
                        background: SG.warningTint,
                        title: 'Offline mode',
                        message: 'SOS alerts stay queued until a connection returns.',
                      ),
                      const SizedBox(height: 16),
                    ],
                    if (wide)
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Expanded(flex: 6, child: _buildSafePoints()),
                          const SizedBox(width: 16),
                          Expanded(flex: 5, child: _buildUtilityColumn()),
                        ],
                      )
                    else ...[
                      _buildSafePoints(),
                      const SizedBox(height: 16),
                      _buildUtilityColumn(),
                    ],
                    const SizedBox(height: 16),
                    if (wide)
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Expanded(child: _buildQueue()),
                          const SizedBox(width: 16),
                          Expanded(child: _buildTimeline()),
                        ],
                      )
                    else ...[
                      _buildQueue(),
                      const SizedBox(height: 16),
                      _buildTimeline(),
                    ],
                    const SizedBox(height: 16),
                    _buildReport(),
                    const SizedBox(height: 18),
                    const Center(
                      child: Text(
                        'Demo screen · sample data only',
                        style: TextStyle(color: SG.muted, fontSize: 12),
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        ),
      ),
    );
  }

  Widget _buildSafePoints() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _cardHeader('Safe points nearby', 'SAMPLE LOCATION'),
            const SizedBox(height: 14),
            Container(
              height: 190,
              decoration: BoxDecoration(
                color: const Color(0xFF171D1B),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: SG.border),
              ),
              child: Stack(
                children: [
                  Positioned.fill(
                    child: CustomPaint(painter: _MapRoadPainter()),
                  ),
                  const Positioned(
                    left: 22,
                    top: 24,
                    child: _MapTag(
                      icon: Icons.local_police_outlined,
                      text: 'Police',
                    ),
                  ),
                  const Positioned(
                    right: 20,
                    top: 86,
                    child: _MapTag(
                      icon: Icons.local_hospital_outlined,
                      text: 'Hospital',
                    ),
                  ),
                  const Positioned(
                    left: 52,
                    bottom: 27,
                    child: _MapTag(
                      icon: Icons.shield_outlined,
                      text: 'Security',
                    ),
                  ),
                  Center(
                    child: Container(
                      height: 22,
                      width: 22,
                      decoration: BoxDecoration(
                        color: SG.accent,
                        shape: BoxShape.circle,
                        border: Border.all(color: SG.text, width: 4),
                        boxShadow: const [
                          BoxShadow(color: Color(0x6636C99A), blurRadius: 14),
                        ],
                      ),
                    ),
                  ),
                  const Positioned(
                    left: 10,
                    bottom: 9,
                    child: Text(
                      'Map preview · connect map provider for live tiles',
                      style: TextStyle(color: SG.secondary, fontSize: 10),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 8),
            ..._safePoints.map((point) => _SafePointTile(point: point)),
          ],
        ),
      ),
    );
  }

  Widget _buildUtilityColumn() {
    return Column(
      children: [
        _buildJourney(),
        const SizedBox(height: 16),
        _buildCheckIn(),
      ],
    );
  }

  Widget _buildJourney() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _cardHeader('Journey mode', 'TEMPORARY LINK'),
            const SizedBox(height: 15),
            Row(
              children: [
                const _IconBox(icon: Icons.route_outlined, color: SG.accent),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        _journeyActive
                            ? 'Link active · ${_formatTime(_journeySeconds)}'
                            : 'Share your journey',
                        style: const TextStyle(
                          color: SG.text,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        _journeyActive
                            ? 'Sample contact can view the link.'
                            : 'Let a trusted contact follow your trip.',
                        style: const TextStyle(
                          color: SG.secondary,
                          fontSize: 12,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 14),
            SizedBox(
              width: double.infinity,
              child: _journeyActive
                  ? OutlinedButton.icon(
                      onPressed: _stopJourney,
                      icon: const Icon(Icons.link_off),
                      label: const Text('Revoke demo link'),
                      style: OutlinedButton.styleFrom(
                        foregroundColor: SG.danger,
                        side: const BorderSide(color: SG.border),
                        minimumSize: const Size.fromHeight(46),
                      ),
                    )
                  : FilledButton.icon(
                      onPressed: _startJourney,
                      icon: const Icon(Icons.share_location_outlined),
                      label: const Text('Start journey'),
                      style: FilledButton.styleFrom(
                        backgroundColor: SG.accent,
                        foregroundColor: SG.background,
                        minimumSize: const Size.fromHeight(46),
                      ),
                    ),
            ),
            if (_journeyActive) ...[
              const SizedBox(height: 12),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(11),
                decoration: BoxDecoration(
                  color: const Color(0xFF191D22),
                  borderRadius: BorderRadius.circular(9),
                ),
                child: const SelectableText(
                  'https://CyberSuraksha.example/journey/demo',
                  style: TextStyle(color: SG.secondary, fontSize: 12),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildCheckIn() {
    final warning = _checkInActive && _checkInSeconds <= 120;
    final color = warning ? SG.warning : SG.accent;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _cardHeader('Safety check-in', '15 MINUTES'),
            const SizedBox(height: 14),
            Row(
              children: [
                _IconBox(icon: Icons.timer_outlined, color: color),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    _checkInActive
                        ? 'Confirm you are safe'
                        : 'Set a check-in timer',
                    style: const TextStyle(
                      color: SG.text,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
                if (_checkInActive)
                  Text(
                    _formatTime(_checkInSeconds),
                    style: TextStyle(
                      color: color,
                      fontSize: 20,
                      fontWeight: FontWeight.w700,
                      fontFeatures: const [FontFeature.tabularFigures()],
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: _checkInActive
                  ? FilledButton.icon(
                      onPressed: _confirmSafe,
                      icon: const Icon(Icons.check_circle_outline),
                      label: const Text('I’m safe'),
                      style: FilledButton.styleFrom(
                        backgroundColor: SG.accent,
                        foregroundColor: SG.background,
                        minimumSize: const Size.fromHeight(46),
                      ),
                    )
                  : OutlinedButton.icon(
                      onPressed: _startCheckIn,
                      icon: const Icon(Icons.play_arrow),
                      label: const Text('Start check-in'),
                      style: OutlinedButton.styleFrom(
                        foregroundColor: SG.text,
                        side: const BorderSide(color: SG.border),
                        minimumSize: const Size.fromHeight(46),
                      ),
                    ),
            ),
            if (_checkInActive)
              const Padding(
                padding: EdgeInsets.only(top: 9),
                child: Text(
                  'Demo timer only; connect to the emergency escalation service.',
                  style: TextStyle(color: SG.muted, fontSize: 11),
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildQueue() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _cardHeader('Offline SOS queue', '${_pendingAlerts.length} PENDING'),
            const SizedBox(height: 10),
            if (_pendingAlerts.isEmpty)
              const Padding(
                padding: EdgeInsets.symmetric(vertical: 14),
                child: Row(
                  children: [
                    Icon(Icons.check_circle_outline, color: SG.accent),
                    SizedBox(width: 10),
                    Text('No pending alerts', style: TextStyle(color: SG.secondary)),
                  ],
                ),
              )
            else
              ..._pendingAlerts.asMap().entries.map(
                (entry) => Padding(
                  padding: const EdgeInsets.only(top: 8),
                  child: Row(
                    children: [
                      const Icon(Icons.circle, size: 9, color: SG.warning),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              entry.value,
                              style: const TextStyle(color: SG.text, fontSize: 13),
                            ),
                            const SizedBox(height: 3),
                            const Text(
                              'Stored on this device · sample item',
                              style: TextStyle(color: SG.muted, fontSize: 11),
                            ),
                          ],
                        ),
                      ),
                      TextButton(
                        onPressed: () => _retryAlert(entry.key),
                        child: const Text('Retry'),
                      ),
                    ],
                  ),
                ),
              ),
            if (!_online && _pendingAlerts.isNotEmpty)
              const Padding(
                padding: EdgeInsets.only(top: 8),
                child: Text(
                  'Not sent yet. It will remain queued while offline.',
                  style: TextStyle(color: SG.warning, fontSize: 11),
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildTimeline() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _cardHeader('Incident timeline', 'SG-2048'),
            const SizedBox(height: 8),
            ..._events.map(
              (event) => Padding(
                padding: const EdgeInsets.symmetric(vertical: 8),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Padding(
                      padding: EdgeInsets.only(top: 4),
                      child: Icon(Icons.circle, size: 9, color: SG.accent),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            event.title,
                            style: const TextStyle(
                              color: SG.text,
                              fontSize: 13,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                          const SizedBox(height: 3),
                          Text(
                            event.detail,
                            style: const TextStyle(color: SG.secondary, fontSize: 11),
                          ),
                        ],
                      ),
                    ),
                    Text(
                      event.time,
                      style: const TextStyle(color: SG.muted, fontSize: 10),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildReport() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Row(
          children: [
            const _IconBox(
              icon: Icons.description_outlined,
              color: SG.secondary,
            ),
            const SizedBox(width: 12),
            const Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Evidence report',
                    style: TextStyle(color: SG.text, fontWeight: FontWeight.w600),
                  ),
                  SizedBox(height: 4),
                  Text(
                    'Create a report for this incident.',
                    style: TextStyle(color: SG.secondary, fontSize: 12),
                  ),
                ],
              ),
            ),
            OutlinedButton(
              onPressed: () =>
                  _showMessage('Demo only: connect the evidence report API.'),
              style: OutlinedButton.styleFrom(
                foregroundColor: SG.text,
                side: const BorderSide(color: SG.border),
              ),
              child: const Text('Create'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _cardHeader(String title, String meta) {
    return Row(
      children: [
        Expanded(
          child: Text(
            title,
            style: const TextStyle(
              color: SG.text,
              fontSize: 16,
              fontWeight: FontWeight.w600,
            ),
          ),
        ),
        Text(
          meta,
          style: const TextStyle(
            color: SG.muted,
            fontSize: 9,
            letterSpacing: 1,
            fontWeight: FontWeight.w600,
          ),
        ),
      ],
    );
  }
}

class _SafePoint {
  const _SafePoint(this.name, this.type, this.distance, this.icon);

  final String name;
  final String type;
  final String distance;
  final IconData icon;
}

class _IncidentEvent {
  const _IncidentEvent(this.time, this.title, this.detail);

  final String time;
  final String title;
  final String detail;
}

class _SafePointTile extends StatelessWidget {
  const _SafePointTile({required this.point});

  final _SafePoint point;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 7),
      child: Row(
        children: [
          _IconBox(icon: point.icon, color: SG.accent),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  point.name,
                  style: const TextStyle(
                    color: SG.text,
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                Text(
                  point.type,
                  style: const TextStyle(color: SG.secondary, fontSize: 11),
                ),
              ],
            ),
          ),
          Text(point.distance, style: const TextStyle(color: SG.secondary, fontSize: 11)),
        ],
      ),
    );
  }
}

class _IconBox extends StatelessWidget {
  const _IconBox({required this.icon, required this.color});

  final IconData icon;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 40,
      width: 40,
      decoration: BoxDecoration(
        color: const Color(0xFF191D22),
        borderRadius: BorderRadius.circular(11),
      ),
      child: Icon(icon, color: color, size: 20),
    );
  }
}

class _MapTag extends StatelessWidget {
  const _MapTag({required this.icon, required this.text});

  final IconData icon;
  final String text;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 6),
      decoration: BoxDecoration(
        color: SG.surface,
        borderRadius: BorderRadius.circular(9),
        border: Border.all(color: SG.border),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 14, color: SG.accent),
          const SizedBox(width: 5),
          Text(
            text,
            style: const TextStyle(
              color: SG.text,
              fontSize: 10,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}

class _StatusBanner extends StatelessWidget {
  const _StatusBanner({
    required this.icon,
    required this.color,
    required this.background,
    required this.title,
    required this.message,
  });

  final IconData icon;
  final Color color;
  final Color background;
  final String title;
  final String message;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: background,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: color.withValues(alpha: 0.3)),
      ),
      child: Row(
        children: [
          Icon(icon, color: color),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: TextStyle(color: color, fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 3),
                Text(
                  message,
                  style: const TextStyle(color: SG.secondary, fontSize: 12),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _MapRoadPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final minor = Paint()
      ..color = const Color(0xFF343D3A)
      ..strokeWidth = 7
      ..strokeCap = StrokeCap.round;

    final major = Paint()
      ..color = const Color(0xFF59645F)
      ..strokeWidth = 10
      ..strokeCap = StrokeCap.round;

    canvas.drawLine(
      Offset(-20, size.height * .72),
      Offset(size.width * .78, -15),
      minor,
    );
    canvas.drawLine(
      Offset(size.width * .18, size.height + 20),
      Offset(size.width + 20, size.height * .22),
      major,
    );
    canvas.drawLine(
      Offset(-10, size.height * .30),
      Offset(size.width + 15, size.height * .58),
      minor,
    );

    final park = Paint()..color = const Color(0xFF15362B);
    canvas.drawRRect(
      RRect.fromRectAndRadius(
        Rect.fromLTWH(
          size.width * .68,
          size.height * .02,
          size.width * .27,
          size.height * .24,
        ),
        const Radius.circular(18),
      ),
      park,
    );
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}