package shapes

import org.geotools.data.DataStore
import org.geotools.data.DataStoreFinder
import org.geotools.data.FeatureSource
import org.geotools.data.simple.SimpleFeatureIterator
import org.geotools.feature.FeatureCollection
import org.geotools.feature.FeatureIterator
import org.geotools.filter.Filter
import org.geotools.filter.text.cql2.CQL
import org.opengis.feature.Feature
import spock.lang.Specification

class ReadShapeFileTest extends Specification {

    def readFile(){
        given:
            File file = new File("src/test/resources/tl_2013_us_zcta510.shp")
            Map map = new HashMap()
            map.put( "url", file.toURL() )
        when:
            DataStore dataStore = DataStoreFinder.getDataStore( map )
            String typeName = dataStore.getTypeNames()[0]
            FeatureSource source = dataStore.getFeatureSource( typeName )
//            Filter filter = CQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")   ZCTA5CE10    between 90000-96199
            Filter filter = CQL.toFilter("ZCTA5CE10 BETWEEN 90000 AND 96199")
            FeatureCollection collection = source.getFeatures( filter )
//            FeatureCollection collection = source.getFeatures()
            SimpleFeatureIterator iterator = collection.features()
            Feature feature
            def i = 0
        try {
            while( iterator.hasNext() ){
                i++
                feature = (Feature) iterator.next()
            }
        }
        finally {
            iterator.close()
        }
        then:
            println i
            i > 0            //33144 no filter
    }

}
