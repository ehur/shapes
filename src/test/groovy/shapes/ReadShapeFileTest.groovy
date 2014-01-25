package shapes

import org.geotools.data.DataStore
import org.geotools.data.DataStoreFinder
import org.geotools.data.DefaultTransaction
import org.geotools.data.FeatureSource
import org.geotools.data.Transaction
import org.geotools.data.shapefile.ShapefileDataStore
import org.geotools.data.shapefile.ShapefileDataStoreFactory
import org.geotools.data.simple.SimpleFeatureIterator
import org.geotools.data.simple.SimpleFeatureSource
import org.geotools.data.simple.SimpleFeatureStore
import org.geotools.feature.FeatureCollection
import org.geotools.feature.simple.SimpleFeatureTypeImpl
import org.geotools.filter.Filter
import org.geotools.filter.text.cql2.CQL
import org.opengis.feature.Feature
import org.opengis.feature.simple.SimpleFeatureType
import org.opengis.feature.type.FeatureType
import spock.lang.Specification

class ReadShapeFileTest extends Specification {

    def readFile(){
        given:
            File file = new File("src/test/resources/tl_2013_us_zcta510.shp")
            Map map = new HashMap()
            map.put( "url", file.toURL() )
        when:
        FeatureCollection californiaCollection = getCaliforniaCollection(map)
//            FeatureCollection collection = source.getFeatures()
            SimpleFeatureIterator iterator = californiaCollection.features()
            Feature feature
            def i = 0
        Set<Feature> featureSet = new HashSet<Feature>()
        try {
            while( iterator.hasNext() ){
                i++
                feature = (Feature) iterator.next()
                featureSet.add(feature)
            }
        }
        finally {
            iterator.close()
        }
        then:
            println i
            i > 0            //33144 no filter
            featureSet.each() {fea -> println fea.getType()}
    }


    def writeFile(){
        given:
            File file = new File("src/test/resources/tl_2013_us_zcta510.shp")
            Map map = new HashMap()
            map.put( "url", file.toURL() )
            FeatureCollection californiaCollection = getCaliforniaCollection(map)
            FeatureType featureType = californiaCollection.getSchema()
            ShapefileDataStore dataStore = getFeatureStoreForFile(new File("target/ca_zipcodes.shp"), featureType)
            String typeName = dataStore.getTypeNames()[0]
        when:
            SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
        then:
            featureSource instanceof SimpleFeatureStore
        when:
            Transaction transaction = new DefaultTransaction("create");
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(californiaCollection);
                transaction.commit();

            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();

            } finally {
                transaction.close();
            }
        then:
             File outputFile = new File("target/ca_zipcodes.shp")
            outputFile.exists()
    }

    private ShapefileDataStore getFeatureStoreForFile(File targetFile,FeatureType featureType){

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory()

        Map<String, Serializable> params = new HashMap<String, Serializable>()
        params.put("url", targetFile.toURI().toURL())
        params.put("create spatial index", Boolean.TRUE)
        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params)
        newDataStore.createSchema(featureType)
//        newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84)
        return newDataStore
    }


    private FeatureCollection getCaliforniaCollection(Map map){
        DataStore dataStore = DataStoreFinder.getDataStore( map )
        String typeName = dataStore.getTypeNames()[0]
        FeatureSource source = dataStore.getFeatureSource( typeName )
        Filter filter = CQL.toFilter("ZCTA5CE10 BETWEEN 90000 AND 96199")
        return source.getFeatures( filter )
    }

}
