public interface EntryStrategy{
    /**
     *
     * @return Position entry if purchased coins else null.
     */
    PositionEntry run();
}
