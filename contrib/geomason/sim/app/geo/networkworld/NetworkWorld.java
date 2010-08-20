package sim.app.geo.networkworld;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.planargraph.Node;
import java.util.Iterator;
import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.util.geo.*;

/** Set up a GeoField with a number of points and a corresponding portrayal.
 *
 * @author mcoletti
 */
public class NetworkWorld extends SimState
{

    private static final long serialVersionUID = 2025934565604118804L;
	public GeomVectorField world = new GeomVectorField(); // contains road network
    public GeomVectorField junctions = new GeomVectorField(); // nodes for intersections
    public GeomVectorField agents = new GeomVectorField(); // agents moving through network

    // Stores transportation network connections
    public GeomPlanarGraph network = new GeomPlanarGraph();

    // Agent that moves around the world
    Agent a = new Agent();

    public NetworkWorld(long seed) throws ParseException
    {
        super(seed);

        // Add a lines and a polygon

        WKTReader rdr = new WKTReader();
        LineString line = null;

        try
            {
                line = (LineString) (rdr.read("LINESTRING (10 50, 20 50)"));
                world.addGeometry(new MasonGeometry(line));

                line = (LineString) (rdr.read("LINESTRING (20 50, 30 50)"));
                world.addGeometry(new MasonGeometry(line));

                line = (LineString) (rdr.read("LINESTRING (30 50, 40 50)"));
                world.addGeometry(new MasonGeometry(line));

                line = (LineString) (rdr.read("LINESTRING (20 50, 20 10, 30 10)"));
                world.addGeometry(new MasonGeometry(line));

                line = (LineString) (rdr.read("LINESTRING (30 50, 30 20, 40 20)"));
                world.addGeometry(new MasonGeometry(line));
            
                // zoom out to see all of line
                Envelope mbr = world.getMBR();
                mbr.expandToInclude(0.0, 0.0);

                agents.addGeometry(new MasonGeometry(a.getGeometry()));
                mbr.expandToInclude(agents.getMBR());

                mbr.expandBy(20.0); // fluff it out so we can see everything

                agents.setMBR(mbr);
                world.setMBR(mbr);
            }
        catch (ParseException parseException)
            {
                System.out.println("Bogus line string");
            }

        network.createFromGeomField(world);
        addIntersectionNodes( network.nodeIterator(), junctions) ;
    }


    /** adds nodes corresponding to road intersections to GeomVectorField
     *
     * @param nodeIterator Points to first node
     * @param intersections GeomVectorField containing intersection geometry
     *
     * Nodes will belong to a planar graph populated from LineString network.
     */
    private void addIntersectionNodes(Iterator<?> nodeIterator, GeomVectorField intersections)
    {
        GeometryFactory fact = new GeometryFactory();
        Coordinate coord = null;
        Point point = null;

        while (nodeIterator.hasNext())
            {
                Node node = (Node) nodeIterator.next();
                System.out.println("node: " + node.getCoordinate() + " " + node.getDegree());
                coord = node.getCoordinate();
                point = fact.createPoint(coord);
                junctions.addGeometry(new MasonGeometry(point));
            }
    }

    public void start()
    {
        super.start();
        a.start(this);
        schedule.scheduleRepeating(a);
    }
    

    void addPoint(final double x, final double y)
    {
        GeometryFactory fact = new GeometryFactory(); // XXX consider making this static member
        Point location = fact.createPoint(new Coordinate(x, y));
        world.addGeometry(new MasonGeometry(location));
    }

    public static void main(String[] args)
    {
        doLoop(NetworkWorld.class, args);
        System.exit(0);
    }
}
