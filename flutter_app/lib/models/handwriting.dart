enum ModelStatus { notDownloaded, downloading, downloaded, failed }

class HandwritingPoint {
  final double x;
  final double y;
  final int t;

  HandwritingPoint({required this.x, required this.y, required this.t});
}

class HandwritingStroke {
  final List<HandwritingPoint> points = [];
}
