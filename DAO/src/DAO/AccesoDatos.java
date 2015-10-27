package DAO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.naming.NamingException;

/*
 * @(#)AccesoDatos.java   1.0 14/02/2015
 *
 * Copyright (c) 2015
 *
 * Este Software es cofidencial y contiene informaci�n de importancia para la
 * Universidad Tecnica Nacional. La  misma debe ser usada
 * con discreci�n y bajo permiso de la instituci�n
 *
 */

/**
 * Clase que provee el Acceso a Datos para la Aplicaci�n.
 * 
 * @version 1.0 14/2/2014
 * @author Jose Herrera
 */


public class AccesoDatos implements Serializable {
   
    /*Constante que identifica el motor de la base de datos*/
     static final int SQL_SERVER = 1;
     static final int ORACLE = 2;
     static final int MY_SQL = 3;
    
    /*Constantes que identifican el resultado de la operacion*/
     static final int OPERACION_EFECTUADA = 1;
     static final int ERROR_EJECUCION = 0;
    @SuppressWarnings("compatibility:-5753742077201878016")
    private static final long serialVersionUID = 1L;
    private transient Connection dbConn = null;
    private transient Statement stmt = null;

        
        /**
         * Ejecuta una instrucci�n SQL
         * @param pvcSQL
         * @return
         * @throws FileNotFoundException
         * @throws IOException
         * @throws SQLException
         * @throws ClassNotFoundException
         */
        
        public int ejecutaSQL(String pvcSQL)throws SICOExceptions, SQLException, NamingException  {
                       
            dbConn = DriverManager.getConnection("jdbc:weblogic:sqlserver://localhost:1433;databaseName=BD_SICO", "sa", "123456");
            try{
                
                stmt = dbConn.createStatement();
                
                if(getDataSource().equals("jdbc/SNMPDataSourceDB")){
                    String formatoFechaSQL = "Alter session set NLS_DATE_FORMAT = 'dd-mm--yy'";
                    stmt.execute(formatoFechaSQL);
                    }
                System.out.println("EjecutaSQL: "+pvcSQL);
                stmt.execute(pvcSQL);
                return OPERACION_EFECTUADA;
                }
            catch(SQLException e){
                System.out.println("SQL ERROR: "+pvcSQL);
                System.out.println("SQL EXCEPTION: "+e.getMessage());     
                
                throw new SICOExceptions(SICOExceptions.SQL_EXCEPTION, e.getMessage(), e.getErrorCode());
                }
            finally{
                try{
                    stmt.close();
                    }
                catch(SQLException e){
                        System.out.println("SQL ERROR: "+pvcSQL);
                        System.out.println("SQL EXCEPTION: "+e.getMessage());     
                        throw new SICOExceptions(SICOExceptions.SQL_EXCEPTION, e.getMessage(), e.getErrorCode());
                    }
                }
            }
        
    /**
    * Ejecuta una instrucci�n SQL y retorna un ResultSet
    * @param pvcSQL
    * @return
    * @throws IOException
    * @throws SQLException
    * @throws ClassNotFoundException
    */
    public ResultSet ejecutaSQLRetornaRS(String pvcSQL) throws SICOExceptions, SQLException, NamingException {
      ResultSet rs = null;
      dbConn = DriverManager.getConnection("jdbc:weblogic:sqlserver://localhost:1433;databaseName=BD_SICO", "sa", "123456");
      try {
              stmt = dbConn.createStatement();

              if(getDataSource().equals("jdbc/SNMPDataSourceDB")){ 
                  String formatoFechaSQL = "Alter session set NLS_DATE_FORMAT = 'dd-mm-yy' ";
                stmt.execute(formatoFechaSQL);
                }            
          
               /* Ejecuta la sentencia SQL */
               System.out.println("EjecutaSQLRetornaRS: " + pvcSQL);
               rs = stmt.executeQuery(pvcSQL);
               return rs;
          } catch (SQLException e) {
                System.out.println("SQL ERROR: " + pvcSQL);
                System.out.println("SQL EXCEPTION: " + e.getMessage());
                throw new SICOExceptions(SICOExceptions.SQL_EXCEPTION, 
                                  e.getMessage(), e.getErrorCode());
                }
            finally{
          
          }
    }

    /**
    * Ejecuta una matriz de sentencias SQL
    * @param pvcSQL
    * @return
    * @throws FileNotFoundException
    * @throws IOException
    * @throws SQLException
    * @throws ClassNotFoundException
    */
    public int ejecutaMatrizSQL(String pvcSQL, String pvcSQLBitacora) throws SICOExceptions {
       
       String strSQL[] = null;
       int i = 0;
       
       try {
           
           /* Obtiene la conexi�n hacia la base de datos */
           stmt = dbConn.createStatement();
           
           if(getDataSource().equals("jdbc/SNMPDataSourceDB")){
               String formatoFechaSQL = "Alter session set NLS_DATE_FORMAT = 'dd-mm-yy'";
               stmt.execute(formatoFechaSQL);
           }         
       
           
           /* Se divide el string en varios substrings */
           strSQL = pvcSQL.split(";");
           /* Se establece el AutoCommit de la base de datos en falso
              para iniciar una transacci�n */
           iniciarTransaccion();
           for (i = 0; i < strSQL.length; i++) {
               /* Se ejecuta la sentencia SQL */
               if (!strSQL[i].toString().equals("")) {
                       if (!strSQL[i].toString().equals(" ")) {
                       stmt.execute(strSQL[i].replace(';', ' '));
                   }
               }
           }
           /* Se hace commit a la transacci�n para finalizar la ejecuci�n de
              las sentencias SQL */
          cerrarTransaccion(true);

          stmt.execute(pvcSQLBitacora);            
           
          return OPERACION_EFECTUADA;
       } catch (SQLException e) {
           System.out.println("SQL ERROR: " + strSQL[i] + " BITACORA: " + pvcSQLBitacora);
           System.out.println("SQL EXCEPTION: " + e.getMessage());            
           cerrarTransaccion(false);
           throw new SICOExceptions(SICOExceptions.SQL_EXCEPTION, 
                                   e.getMessage(), e.getErrorCode());
       } finally {
           try {
               stmt.close();
           } catch (SQLException e) {
               System.out.println("SQL ERROR: " + strSQL[i] + " BITACORA: " + pvcSQLBitacora);
               System.out.println("SQL EXCEPTION: " + e.getMessage());                
               throw new SICOExceptions(SICOExceptions.SQL_EXCEPTION, 
                                       e.getMessage(), e.getErrorCode());
           }
       }
    }


    /**
    * Ejecuta una matriz de sentencias SQL
    * @param pvcSQL
    * @return
    * @throws FileNotFoundException
    * @throws IOException
    * @throws SQLException
    * @throws ClassNotFoundException
    */
    public int ejecutaMatrizSQL(String pvcSQL) throws SICOExceptions {

       String strSQL[] = null;
       int i = 0;
       
       try {
           
           /* Obtiene la conexi�n hacia la base de datos */
           stmt = dbConn.createStatement();
           
           if(getDataSource().equals("jdbc/SNMPDataSourceDB")){
               String formatoFechaSQL = "Alter session set NLS_DATE_FORMAT = 'dd-mm-yy'";
               stmt.execute(formatoFechaSQL);
           }         

           /* Se divide el string en varios substrings */
           strSQL = pvcSQL.split(";");

           /* Se establece el AutoCommit de la base de datos en falso
              para iniciar una transacci�n */
           iniciarTransaccion();
           for (i = 0; i < strSQL.length; i++) {
               /* Se ejecuta la sentencia SQL */
               if (!strSQL[i].equals("")) {
                   stmt.execute(strSQL[i]);
               }
           }
           /* Se hace commit a la transacci�n para finalizar la ejecuci�n de
              las sentencias SQL */
           cerrarTransaccion(true);
           return OPERACION_EFECTUADA;
           
       } catch (SQLException e) {
           System.out.println("SQL ERROR: " + strSQL[i]);
           System.out.println("SQL EXCEPTION: " + e.getMessage());            
           cerrarTransaccion(false);
           throw new SICOExceptions(SICOExceptions.SQL_EXCEPTION, 
                                   e.getMessage(), e.getErrorCode());
       } finally {
           try {
               stmt.close();
           } catch (SQLException e) {
               System.out.println("SQL ERROR: " + strSQL[i]);
               System.out.println("SQL EXCEPTION: " + e.getMessage());                
               throw new SICOExceptions(SICOExceptions.SQL_EXCEPTION, 
                                       e.getMessage(), e.getErrorCode());
           }
       }
    }

    /**
    * Ejecuta una instrucci�n SQL y retorna un ResultSet
    * @param pstSQL
    * @return
    * @throws IOException
    * @throws SQLException
    * @throws ClassNotFoundException
    */
    public ResultSet ejecutaSQLRetornaRS(PreparedStatement pstSQL) throws SICOExceptions {
    ResultSet rs = null;
    
    try {
        stmt = dbConn.createStatement();

        if(getDataSource().equals("jdbc/SNMPDataSourceDB")){
           String formatoFechaSQL = "Alter session set NLS_DATE_FORMAT = 'dd-mm-yy' ";
           stmt.execute(formatoFechaSQL);
        }            
       
        /* Ejecuta la sentencia SQL */
        System.out.println("EjecutaSQLRetornaRS: " + pstSQL.toString());
        rs = pstSQL.executeQuery();
        return rs;
        } catch (SQLException e) {
            System.out.println("SQL ERROR: " + pstSQL.toString());
            System.out.println("SQL EXCEPTION: " + e.getMessage());
            throw new SICOExceptions(SICOExceptions.SQL_EXCEPTION, 
                               e.getMessage(), e.getErrorCode());
        }
    }

    /**
    * Cierra un Statement
    * @throws SQLException
    */
    public void cerrarStatements() throws SICOExceptions {
       try {
           stmt.close();
       } catch (SQLException e) {
           System.out.println("SQL EXCEPTION: " + e.getMessage());
           throw new SICOExceptions(SICOExceptions.SQL_EXCEPTION, 
                                   e.getMessage(), e.getErrorCode());
       }
    }

    /**
     * Cierra un ResultSet
     * @param resultado
     * @throws SICOExceptions
     */
    public void closeResultSet(ResultSet resultado) throws SICOExceptions {
       try {
           if (resultado != null) {
               resultado.close();
               resultado = null;
           }
       } catch (SQLException e) {
           System.out.println("SQL EXCEPTION: " + e.getMessage());
           throw new SICOExceptions(SICOExceptions.SQL_EXCEPTION, 
                                   e.getMessage(), e.getErrorCode());
       }
    }

    /**
    * Inicia una transacci�n en la base de datos.
    * @throws SQLException
    */
    public void iniciarTransaccion() throws SICOExceptions {

       try {
           dbConn.setAutoCommit(false);
       } catch (SQLException e) {
           System.out.println("SQL EXCEPTION: " + e.getMessage());
           throw new SICOExceptions(SICOExceptions.SQL_EXCEPTION, 
                                   e.getMessage(), e.getErrorCode());
       }

    }

    /**
    * Cierra una transacci�n en la base de datos.
    * @throws SQLException
    */
    public void cerrarTransaccion(boolean blnExito) throws SICOExceptions {
       try {
           if (blnExito == true) {
               dbConn.commit();
           } else {
               dbConn.rollback();
           }
       } catch (SQLException e) {
           System.out.println("SQL EXCEPTION: " + e.getMessage());
           throw new SICOExceptions(SICOExceptions.SQL_EXCEPTION, 
                                   e.getMessage(), e.getErrorCode());
       }
    }



    public void setTransaccionSerializable() throws SICOExceptions
    {
       try
       {
           dbConn.setTransactionIsolation(dbConn.TRANSACTION_SERIALIZABLE);
       }
       catch(SQLException e)
       {
           System.out.println("SQL EXCEPTION: " + e.getMessage());
           throw new SICOExceptions(SICOExceptions.SQL_EXCEPTION, e.getMessage(), e.getErrorCode());
       }
    }

    public void setTransaccionNone() throws SICOExceptions
    {
       try
       {
           dbConn.setTransactionIsolation(dbConn.TRANSACTION_NONE);
       }
       catch(SQLException e)
       {
           System.out.println("SQL EXCEPTION: " + e.getMessage());
           throw new SICOExceptions(SICOExceptions.SQL_EXCEPTION, e.getMessage(), e.getErrorCode());
       }
    }

    /**
    * Cierra la conexi�n con la base de datos.
    * @throws SQLException
    */
    public void cerrarConexion() throws SICOExceptions {

       try {
           if(dbConn != null) {
               dbConn.close();
               dbConn = null;
           }
       } catch (SQLException e) {
           System.out.println("SQL EXCEPTION: " + e.getMessage());
           throw new SICOExceptions(SICOExceptions.SQL_EXCEPTION, 
                                   e.getMessage(), e.getErrorCode());
       }
    }

    /**
    * Deshace los cambios hechos dentro de una transacci�n.
    * @throws SQLException
    */
    public void rollbackTransaccion() throws SICOExceptions {
       try {
           dbConn.rollback();
       } catch (SQLException e) {
           System.out.println("SQL EXCEPTION: " + e.getMessage());
           throw new SICOExceptions(SICOExceptions.SQL_EXCEPTION, 
                                   e.getMessage(), e.getErrorCode());
       }
    }

    public void setDbConn(Connection dbConn) {
       this.dbConn = dbConn;
    }

    public Connection getDbConn() {
       return dbConn;
    }
    
    
    public String getDataSource(){
             
       String ds = "";
       
       try {

           Properties properties = new Properties();
           String pathToConfigurationFile = "Configuracion.properties";
           
           //Carga del archivo de configuraci�n
           FileInputStream input = new FileInputStream(pathToConfigurationFile);
           properties.load(input);
           
           ds = properties.getProperty("datasource");   
           System.out.println("Data Source: " + ds);
           //dataSource = (DataSource) context.lookup(ds);

       }
       catch (IOException e) {
           System.out.println("SQL EXCEPTION: " + e.getMessage());
           System.out.println("ERROR: "+ e.getMessage());
       }
       return ds;
       
    }


    public AccesoDatos() {
        super();
    }
}
