package nl.adamg.baizel.internal.common.annotations;

/// Marks class that:
/// - is serializable
/// - is non-final
/// - has all fields public, non-static, non-final
/// - has the canonical public constructor accepting parameter for each field
/// - has canonical hashCode and equals, including every field
/// - contains no other methods
public @interface Entity {
}
