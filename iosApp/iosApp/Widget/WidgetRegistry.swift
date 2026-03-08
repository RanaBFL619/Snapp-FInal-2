import SwiftUI
import shared

/// Widget registry — same role as web client/src/lib/widgetRegistry.ts (Map<type, Component>).
/// WidgetRenderer gets component via widgetRegistry.get(widget.type); we get builder via get(type) and call with WidgetRenderContext.
/// Keys must match API widget.type and web registry exactly (bar-chart, card, tabs, field, etc.). Web has duplicate keys (stepper, report); we register each once.
final class WidgetRegistry {
    static let shared = WidgetRegistry()
    private var builders: [String: (WidgetRenderContext) -> AnyView] = [:]

    private init() {
        registerAllWidgets()
    }

    func register(_ type: String, builder: @escaping (WidgetRenderContext) -> AnyView) {
        builders[type] = builder
    }

    func get(_ type: String) -> ((WidgetRenderContext) -> AnyView)? {
        builders[type]
    }

    func registerAllWidgets() {
        // Charts
        register("bar-chart") { AnyView(BarChartWidgetView(context: $0)) }
        register("line-chart") { AnyView(LineChartWidgetView(context: $0)) }
        register("area-chart") { AnyView(AreaChartWidgetView(context: $0)) }
        register("pie-chart") { AnyView(PieChartWidgetView(context: $0)) }
        register("donut-chart") { AnyView(DonutChartWidgetView(context: $0)) }
        register("radar-chart") { AnyView(RadarChartWidgetView(context: $0)) }
        register("radial-chart") { AnyView(RadialChartWidgetView(context: $0)) }
        register("bubble-chart") { AnyView(BubbleChartWidgetView(context: $0)) }
        register("funnel-chart") { AnyView(FunnelChartWidgetView(context: $0)) }
        register("candle-chart") { AnyView(CandlestickWidgetView(context: $0)) }
        register("heatmap") { AnyView(HeatMapWidgetView(context: $0)) }

        // Data / list / table
        register("list") { AnyView(TableWidgetView(context: $0)) }
        register("table") { AnyView(ComprehensiveTableWidgetView(context: $0)) }

        // Containers
        register("card") { AnyView(CardWidgetView(context: $0)) }
        register("record") { AnyView(CardWidgetView(context: $0)) }
        register("accordion") { AnyView(AccordionWidgetView(context: $0)) }
        register("tabs") { AnyView(TabWidgetView(context: $0)) }

        // KPI / metric
        register("metric") { AnyView(MetricWidgetView(context: $0)) }

        // Other widgets (exact keys as web)
        register("calendar") { AnyView(CalendarWidgetView(context: $0)) }
        register("chat") { AnyView(ChatWidgetView(context: $0)) }
        register("todo") { AnyView(TodoListWidgetView(context: $0)) }
        register("field") { AnyView(FieldWidgetView(context: $0)) }
        register("importer") { AnyView(ImportWidgetView(context: $0)) }
        register("contact") { AnyView(ContactWidgetView(context: $0)) }
        register("report") { AnyView(ReportsWidgetView(context: $0)) }
        register("rich-text-editor") { AnyView(RichTextEditorWidgetView(context: $0)) }
        register("google-map") { AnyView(GoogleMapWidgetView(context: $0)) }
        register("whiteboard") { AnyView(WhiteboardWidgetView(context: $0)) }
        register("kanban") { AnyView(KanbanWidgetView(context: $0)) }
        register("gantt-chart") { AnyView(GanttChartWidgetView(context: $0)) }
        register("stepper") { AnyView(StepperWidgetView(context: $0)) }
        register("payment-gateway") { AnyView(PaymentGatewayWidgetView(context: $0)) }
        register("carousel") { AnyView(CarouselWidgetView(context: $0)) }
        register("file-upload") { AnyView(FileUploadWidgetView(context: $0)) }
        register("template-builder") { AnyView(TemplateBuilderWidgetView(context: $0)) }
        register("rich-text-display") { AnyView(RichTextDisplayWidgetView(context: $0)) }
        register("image") { AnyView(ImageDisplayWidgetView(context: $0)) }
        register("video") { AnyView(VideoDisplayWidgetView(context: $0)) }
        register("button") { AnyView(ButtonWidgetView(context: $0)) }
        register("file-viewer") { AnyView(FileViewerWidgetView(context: $0)) }
        register("pipeline") { AnyView(PipelineWidgetView(context: $0)) }
        register("activity-feed") { AnyView(ActivityFeedWidgetView(context: $0)) }
        register("i-frame") { AnyView(IFrameWidgetView(context: $0)) }
        register("knowledge-graph") { AnyView(KnowledgeGraphWidgetView(context: $0)) }
    }
}
