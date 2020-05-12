package com.example.busbuddy.communication;


public class BusMessage {
    public String lineCode; // unique id ths grammis
    public String routeCode; // unique id diadromis tou leoforeiou, diaforetiko apo afethria pros terma kai apo terma pros afethria
    public String vehicleID; // unique id tou leoforeiou pou ektelei thn grammi
    public String lineName;
    public String buslineID;
    
    public double latitude; // g platos
    public double longitude; // g mikos
}
