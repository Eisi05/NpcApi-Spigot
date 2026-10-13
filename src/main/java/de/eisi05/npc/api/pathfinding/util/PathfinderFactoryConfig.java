package de.eisi05.npc.api.pathfinding.util;

import de.eisi05.npc.api.pathfinding.algorithm.AbstractPathfinder;
import de.eisi05.npc.api.pathfinding.movement.NavOptions;

public class PathfinderFactoryConfig
{
    private String type;
    private Double gridStep;
    private NavOptions options;

    public PathfinderFactoryConfig() {}

    public PathfinderFactoryConfig(String type)
    {
        this.type = type;
    }

    public PathfinderFactoryConfig(String type, Double gridStep, NavOptions options)
    {
        this.type = type;
        this.gridStep = gridStep;
        this.options = options;
    }

    /**
     * Converts this JSON-friendly config back into the functional interface.
     */
    public AbstractPathfinder.PathfinderFactory<?> createFactory()
    {
        if(type == null)
            return AbstractPathfinder.PathfinderFactory.ASTAR;

        return switch(type.toUpperCase())
        {
            case "ASTAR_GRID" -> AbstractPathfinder.PathfinderFactory.ASTAR_GRID;
            case "BOUNDING_BOX" ->
            {
                if(options != null && gridStep != null)
                    yield AbstractPathfinder.PathfinderFactory.boundingBox(gridStep, options);
                else if(gridStep != null)
                    yield AbstractPathfinder.PathfinderFactory.boundingBox(gridStep);
                yield AbstractPathfinder.PathfinderFactory.BOUNDING_BOX;
            }
            case "ASTAR_CUSTOM" ->
            {
                if(options != null)
                    yield AbstractPathfinder.PathfinderFactory.astar(options);
                yield AbstractPathfinder.PathfinderFactory.ASTAR;
            }
            default -> AbstractPathfinder.PathfinderFactory.ASTAR;
        };
    }

    public String getType() {return type;}

    public Double getGridStep() {return gridStep;}

    public NavOptions getOptions() {return options;}
}