package com.genius.petr.brnomapbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Pair;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petr on 1. 4. 2017.
 */


/*
jak by se to melo chovat:
    - na zacatku zjistit, kde je
    - potom vybrat dva body, mezi kterymi se nachazi
    - zjistit, ke kteremu z nich se priblizuje
    - trackovat postup
    - pokud nektery klicovy bod na trase preskoci, zjistit, jestli se odchylil hodne, nebo ne
        - jestli ne, pridat vsechny klicove body mezi posledni a soucasnou pozici
        - jestli ano, ukoncit soucasny segment a zacit novy - vznikne mezera v okruhu, protoze to tam neni projdute

    - pokud pujde zpatky, tak...?
 */
public class Circuit {
    private List<Place> places;
    private List<MarkerViewOptions> markers;
    private boolean onTrack;
    private int initCounter;
    private Integer lastClosestPointIndex;
    private PathCut lastCut;
    private LatLng lastPosition;

    private static final int DISTANCE_THRESHOLD = 50; //minimal distance to track
    private static final int UPDATE_THRESHOLD = 5; //minimal change to update
    private static final int INIT_STEPS = 3;

    //once both places are visited, color path between them as visited
    private List<LatLng> path;
    private List<Boolean> visitedNodes;
    private List<PathSegment> visitedSegments;
    private PathSegment currentSegment;

    public Circuit(List<LatLng> path, List<Place> places, Context context){
        onTrack = false;
        setInit();
        this.path = path;

        visitedNodes = new ArrayList<>(path.size());
        for (LatLng point : path) {
            visitedNodes.add(false);
        }

        visitedSegments = new ArrayList<>();
        //addTestingSegments();
        setMarkers(places, context);
    }

    private void setMarkers(List<Place> places, Context context) {
        markers = new ArrayList<>(places.size());
        int i = 1;
        for (Place p : places) {
            MarkerIconHelper help = new MarkerIconHelper(context);
            Bitmap icoBitmap = help.getMarkerBitmapWithText(Integer.toString(i));
            Drawable iconDrawable = new BitmapDrawable(context.getResources(), icoBitmap);
            IconFactory iconFactory = IconFactory.getInstance(context);
            Icon ico = iconFactory.fromDrawable(iconDrawable);
            MarkerViewOptions m = new MarkerViewOptions()
                    .position(p.getLocation())
                    .title(p.getName())
                    .snippet(Integer.toString(p.getId()))
                    .anchor(0.5f,1.0f)
                    .icon(ico);
            markers.add(m);
            i++;
        }
    }

    public List<MarkerViewOptions> getMarkers(){
        return markers;
    }

    private void addTestingSegments(){
        PathCut pathCut = new PathCut();
        LatLng destination = path.get(5);
        LatLng second = path.get(6);

        pathCut.setDestination(destination);
        pathCut.setDestinationIndex(5);
        pathCut.setSecondPoint(second);
        pathCut.setSecondPointIndex(6);
        double t = 0.6;
        LatLng cut = new LatLng(destination.getLatitude() + t * (second.getLatitude()-destination.getLatitude()),
                destination.getLongitude() + t * (second.getLongitude()-destination.getLongitude()));
        pathCut.setCut(cut);
        currentSegment = new PathSegment(pathCut);
        visitedSegments.add(currentSegment);
    }

    public List<PathSegment> getVisitedSegments(){
        return visitedSegments;
    }

    public List<LatLng> getPath(){
        return path;
    }

    private void setInit(){
        Log.i("Circuit", "init!");
        initCounter = INIT_STEPS;
        lastCut = null;
        currentSegment = null;
        lastPosition = null;
    }

    private void init(LatLng position) {
        Log.i("Circuit","init: "+initCounter);
        int closestIndex = findClosestPoint(position);
        PathCut cut = findClosestCut(closestIndex, position);

        if (cut == null) {
            setInit();
            return;
        }

        //tady nemuzu tusit, kam smeruje
        if (initCounter != INIT_STEPS) {
            //zajistit, ze lastcut a cut maji stejne okrajove body
            if (cut.getDestinationIndex() != lastCut.getDestinationIndex()) {
                if (cut.getDestinationIndex() == lastCut.getSecondPointIndex() && cut.getSecondPointIndex() == lastCut.getDestinationIndex()) {
                    cut.swapPoints();
                } else {
                    //nemusim nastavovat ani pozici ani rez, protoze to stejne jen resetuju v novem initu
                    Log.i("Circuit","reset 1");
                    setInit();
                    return;
                }
            }

            //po prvni iteraci zjistim, kam se priblizuje
            if (initCounter == INIT_STEPS - 1) {
                //priblizuju se k druhemu bodu
                //if (position.distanceTo(lastCut.getSecondPoint()) < lastPosition.distanceTo(lastCut.getSecondPoint())) {
                if (cut.getCut().distanceTo(lastCut.getSecondPoint()) < lastCut.getCut().distanceTo(lastCut.getSecondPoint())) {
                    //prohodim destination a second point
                    cut.swapPoints();
                }
                currentSegment = new PathSegment(cut);
            } else {
                //zmenil se smer pohybu - zmateni - novy init
                if (cut.getCut().distanceTo(cut.getDestination()) > lastCut.getCut().distanceTo(cut.getDestination())) {
                    Log.i("Circuit","reset 2");
                    setInit();
                    return;
                }
            }
        }
        if(currentSegment != null) {
            currentSegment.update(cut);
        }
        initCounter--;
        lastCut = cut;
        lastPosition = position;

        if (initCounter == 0) {
            visitedSegments.add(currentSegment);
        }
    }

    /*
    if (lastCut.getDestinationIndex() != cut.getDestinationIndex()) {
                    if (lastCut.getDestinationIndex() == cut.getSecondPointIndex() && lastSecondPointIndex == cut.getDestinationIndex()) {
                        lastDestinationPointIndex = cut.getSecondPointIndex();
                        lastSecondPointIndex = cut.getDestinationIndex();
                    }
                } else { //behem inicializace se zmenilo, kde se nachazime na okruhu
                    initCounter = INIT_STEPS;
                    return;
                }
     */

    /*
    private int findSecondPointIndex(LatLng position) {
        double distanceToPrevious;
        double distanceToNext;

        if (pointIndex == 0) {
            distanceToPrevious = position.distanceTo(path.get(path.size()-1));
            distanceToNext = position.distanceTo(path.get(1));
            if (distanceToNext < distanceToPrevious) {
                return 1;
            }
            return path.size()-1;
        }

        if (pointIndex == (path.size()-1)) {
            distanceToPrevious = position.distanceTo(path.get(pointIndex -1));
            distanceToNext = position.distanceTo(path.get(0));
            if (distanceToNext < distanceToPrevious) {
                return 0;
            }
            return pointIndex -1;
        }

        distanceToPrevious = position.distanceTo(path.get(pointIndex -1));
        distanceToNext = position.distanceTo(path.get(pointIndex +1));
        if (distanceToPrevious < distanceToNext) {
            return pointIndex -1;
        }
        else {
            return pointIndex +1;
        }
    }
    */


    private int findClosestPointOnTrack(LatLng position){
        double distance = position.distanceTo(path.get(lastClosestPointIndex));
        int index;

        if (lastClosestPointIndex == 0) {
            index = path.size() - 1;
            if (position.distanceTo(path.get(index)) < distance) {
                return index;
            }

            index = 1;
            if (position.distanceTo(path.get(index)) < distance) {
                return index;
            }
        }

        if (lastClosestPointIndex == path.size()-1) {
            index = 0;
            if (position.distanceTo(path.get(index)) < distance) {
                return index;
            }

            index = lastClosestPointIndex -1;
            if (position.distanceTo(path.get(index)) < distance) {
                return index;
            }
        }

        index = lastClosestPointIndex -1;
        if (position.distanceTo(path.get(index)) < distance) {
            return index;
        }
        index = lastClosestPointIndex +1;
        if (position.distanceTo(path.get(index)) < distance) {
            return index;
        }
        return lastClosestPointIndex;
    }

    private int findClosestPoint(LatLng position) {
        double closestDistance = Double.MAX_VALUE;
        double distance;
        int index = 0;

        for (int i = 0; i<path.size(); i++) {
            distance = position.distanceTo(path.get(i));
            if (distance < closestDistance) {
                index = i;
                closestDistance = distance;
            }
        }
        return index;
    }

    private Pair<Integer, Integer> getAdjacentPointIndexes(int pointIndex){
        if (pointIndex == 0) {
            return new Pair<>(path.size()-1, 1);
        }

        if (pointIndex == (path.size()-1)) {
            return new Pair<>(pointIndex-1, 0);
        }

        return new Pair<>(pointIndex-1, pointIndex+1);
    }

    private PathCut resolveCut(PathCut cut) {
        //TODO: check situation where we cross first/last point in list - indexes change rapidly
        //my circuits are made with plenty of space near first/last point-i'll just ignore it for now

        //check direction
        if (lastCut.getDestinationIndex() > lastCut.getSecondPointIndex()) {
            if (cut.getDestinationIndex() < lastCut.getDestinationIndex()) {
                //going backwards
                Log.i("Circuit","backwards 01");
                return null;
            }
        }
        if (lastCut.getDestinationIndex() < lastCut.getSecondPointIndex()) {
            if (cut.getDestinationIndex() > lastCut.getDestinationIndex()) {
                //going backwards
                Log.i("Circuit","backwards 02");
                return null;
            }
        }

        if (Math.abs(lastCut.getDestinationIndex() - cut.getDestinationIndex()) > Math.abs(lastCut.getDestinationIndex() - cut.getSecondPointIndex())) {
            cut.swapPoints();
        }

        //TODO: reasonable constant - 20?
        //points too far
        if (cut.getSecondPoint().distanceTo(path.get(lastCut.getDestinationIndex())) > 20) {
            Log.i("Circuit","too far");
            return null;
        }

        if (lastCut.getDestinationIndex() > cut.getSecondPointIndex()) {
            for (int i = lastCut.getDestinationIndex(); i > cut.getSecondPointIndex(); i--) {
                currentSegment.addKeypoint(path.get(i));
            }
        } else {
            for (int i = lastCut.getDestinationIndex(); i < cut.getSecondPointIndex(); i++) {
                currentSegment.addKeypoint(path.get(i));
            }
        }

        return cut;
    }

    //TODO: naming in Cut is now confusing
    /*
        given two points in cut it finds which one of them is the destination
     */
    private PathCut resultCut(PathCut cut) {
        if (lastCut == null) {
            return cut;
        }

        if (lastCut.getDestinationIndex() == cut.getDestinationIndex()) {
            //druhy obrazek
            if (lastCut.getSecondPointIndex() == cut.getSecondPointIndex()) {
                Log.i("Circuit","druhy");
                return cut;
            } else { //"destination" is new second point and we are moving out of it
                //ctvrty obrazek
                //nebudu tady resit, jen je potreba pak pridat do segmentu keynode
                cut.swapPoints();
                Log.i("Circuit","ctvrty");
                return cut;
            }
        }

        //prvni obrazek
        if (lastCut.getDestinationIndex() == cut.getSecondPointIndex()) {
            if (lastCut.getSecondPointIndex() == cut.getDestinationIndex()) {
                cut.swapPoints();
                Log.i("Circuit","prvni");
                return cut;
            }
            //obrazek 5 - potreba update, jinak ok
            Log.i("Circuit","paty");
            return cut;
        }

        Log.i("Circuit","out");
        //lastDestination neni ani soucastna destination, ani soucasny druhy bod
        //preskocilo se o moc - zkontrolovat, jestli tam neni nakupenych hodne klicovych bodu u sebe
            // jestli jo, vsechny pridat
            // jestli ne, panikarit a zacit novy init
        //TODO: je to potreba provest tady a pripadne vratit cut
        return resolveCut(cut);
    }

    private PathCut findClosestCut(int closestPointIndex, LatLng position) {
        LatLng closestPoint = path.get(closestPointIndex);
        Pair<Integer, Integer> adjacent = getAdjacentPointIndexes(closestPointIndex);
        LatLng first = path.get(adjacent.first);
        LatLng second = path.get(adjacent.second);

        LatLng firstCut = findClosestCut(closestPoint, first, position);
        LatLng secondCut = findClosestCut(closestPoint, second, position);

        PathCut result = new PathCut();
        result.setDestination(closestPoint);
        result.setDestinationIndex(closestPointIndex);

        boolean firstOK = false;
        boolean secondOK = false;

        Log.i("Circuit","heh");
        //cut is in between
        Log.i("Circuit","closest " + firstCut.distanceTo(closestPoint));
        Log.i("Circuit","first " + firstCut.distanceTo(first));

        if ((firstCut.distanceTo(closestPoint) > 0.1) && (firstCut.distanceTo(first) > 0.1)) {
            firstOK = true;
            Log.i("Circuit","firstOK");
        }
        if ((secondCut.distanceTo(closestPoint) > 0.1) && (secondCut.distanceTo(second) > 0.1)) {
            secondOK = true;
            Log.i("Circuit","secondOK");
        }

        if (firstOK && secondOK) {
            if (firstCut.distanceTo(position) < secondCut.distanceTo(position)) {
                //TODO: mark first point
                result.setSecondPoint(first);
                result.setSecondPointIndex(adjacent.first);
                result.setCut(firstCut);
                return resultCut(result);
            }
            result.setSecondPoint(second);
            result.setSecondPointIndex(adjacent.second);
            result.setCut(secondCut);
            return resultCut(result);
        }

        if (firstOK) {
            result.setSecondPoint(first);
            result.setSecondPointIndex(adjacent.first);
            result.setCut(firstCut);
            return resultCut(result);
        }

        if (secondOK) {
            result.setSecondPoint(second);
            result.setSecondPointIndex(adjacent.second);
            result.setCut(secondCut);
            return resultCut(result);
        }

        //TODO: check that if neither is ok, the result REALLY MUST BE closest point
        result.setSecondPoint(closestPoint);
        result.setSecondPointIndex(closestPointIndex);
        result.setCut(closestPoint);
        return resultCut(result);
    }

    private LatLng findClosestCut(LatLng closestPoint, LatLng secondPoint, LatLng position) {

        double lineLength = closestPoint.distanceTo(secondPoint);
        //sqr(v.x - w.x) + sqr(v.y - w.y)
        //lineLength = Math.sqrt(closestPoint.getLongitude() - secondPoint.getLongitude()) + Math.sqrt(closestPoint.getLatitude()- secondPoint.getLatitude());

        if (lineLength == 0) {
            Log.i("Circuit","0 length");
            return closestPoint;
        }

        /*

        Log.i("Circuit", "closestPoint: "+closestPoint.toString());
        Log.i("Circuit", "secondPoint: " + secondPoint.toString());
        Log.i("Circuit", "position: " + position.toString());

        Log.i("Circuit", "tee: " + String.format("%.12f", (position.getLongitude() - closestPoint.getLongitude())));
        Log.i("Circuit", "tee: " + String.format("%.12f", (secondPoint.getLongitude() - closestPoint.getLongitude())));
        Log.i("Circuit", "tee: " + String.format("%.12f", (position.getLatitude() - closestPoint.getLatitude())));
        Log.i("Circuit", "tee: " + String.format("%.12f", (secondPoint.getLatitude() - closestPoint.getLatitude())));

        //var t = ((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / l2;
        double t = ( ((position.getLongitude() - closestPoint.getLongitude())
                * (secondPoint.getLongitude() - closestPoint.getLongitude()))
                + ((position.getLatitude() - closestPoint.getLatitude())
                * (secondPoint.getLatitude() - closestPoint.getLatitude())))
                / lineLength;
        t = Math.max(0, Math.min(1,t));
        Log.i("Circuit","t = " + String.format("%.12f", t));
        LatLng closestCut = new LatLng(closestPoint.getLatitude() + t * (secondPoint.getLatitude()-closestPoint.getLatitude()),
                closestPoint.getLongitude() + t * (secondPoint.getLongitude()-closestPoint.getLongitude()));
        */
        //return closestCut;
        //again

        /*
        def GetClosestPoint(A, B, P)

  a_to_p = [P.x - A.x, P.y - A.y]     # Storing vector A->P
  a_to_b = [B.x - A.x, B.y - A.y]     # Storing vector A->B

  atb2 = a_to_b[0]**2 + a_to_b[1]**2  # **2 means "squared"
                                      #   Basically finding the squared magnitude
                                      #   of a_to_b

  atp_dot_atb = a_to_p[0]*a_to_b[0] + a_to_p[1]*a_to_b[1]
                                      # The dot product of a_to_p and a_to_b

  t = atp_dot_atb / atb2              # The normalized "distance" from a to
                                      #   your closest point

  return Point.new( :x => A.x + a_to_b[0]*t,
                    :y => A.y + a_to_b[1]*t )
                                      # Add the distance to A, moving
                                      #   towards B

end
         */

        //vectors stored as LatLng...
        LatLng closestToPosition = new LatLng(position.getLatitude() - closestPoint.getLatitude(), position.getLongitude() - closestPoint.getLongitude());
        LatLng closestToSecond = new LatLng(secondPoint.getLatitude() - closestPoint.getLatitude(), secondPoint.getLongitude() - closestPoint.getLongitude());

        double distanceSquared = Math.pow(closestToSecond.getLatitude(),2) + Math.pow(closestToSecond.getLongitude(),2);
        double dot = closestToPosition.getLatitude() * closestToSecond.getLatitude() + closestToPosition.getLongitude() * closestToSecond.getLongitude();

        double t = dot / distanceSquared;
        double absT = Math.abs(t);
        if (t < 0 || t > 1) {
            return closestPoint;
        }

        Log.i("Circuit","t = " + String.format("%.12f", t));

        LatLng closestCut = new LatLng(closestPoint.getLatitude() + t * closestToSecond.getLatitude(),
                closestPoint.getLongitude() + t * closestToSecond.getLongitude());

        return closestCut;
    }

    /*
    private boolean lastAndCurrentClosestPointsAdjacent() {
        if (Math.abs(pointIndex - lastClosestPointIndex) < 2) {
            return true;
        }

        //one of them has to be 0 if they are adjacent and last/first
        if (pointIndex == 0) {
            if (lastClosestPointIndex == path.size()-1) {
                return true;
            }
        }

        if (lastClosestPointIndex == 0) {
            if (pointIndex == path.size()-1) {
                return true;
            }
        }

        return false;
    }
*/


    public void update(LatLng position) {
        if (lastPosition != null && lastPosition.distanceTo(position) < UPDATE_THRESHOLD) {
            return;
        }

        if (initCounter > 0) {
            init(position);
            return;
        }

        //TODO: is finding on track safe? --no, make it safe or dont use it
        /*
        if(onTrack) {
            closestPointIndex = findClosestPointOnTrack(position);
        } else {
            closestPointIndex = findClosestPoint(position);
        }
        */

        int closestPointIndex = findClosestPoint(position);




       // int secondClosestPointIndex = findSecondPointIndex(position);
        //LatLng closestCut = findClosestCut(path.get(closestPointIndex), path.get(secondClosestPointIndex), position);

        PathCut closestCut = findClosestCut(closestPointIndex, position);
        if (closestCut == null) {
            setInit();
            return;
        }

        if(position.distanceTo(closestCut.getCut()) > DISTANCE_THRESHOLD) {
            //onTrack = false;
            setInit();
            return;
        } else {
            //current segment should be abandoned
            if (!currentSegment.update(closestCut)) {
                //visitedSegments.add(currentSegment);
                setInit();
                return;
            }
        }

        //onTrack = true;
        //pointIndex = closestPointIndex;

        lastCut = closestCut;
        lastPosition = position;
    }
}
