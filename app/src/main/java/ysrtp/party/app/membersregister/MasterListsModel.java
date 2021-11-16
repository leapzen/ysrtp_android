package ysrtp.party.app.membersregister;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MasterListsModel {

    @SerializedName("districts")
    @Expose
    private List<Districts> districtsList = new ArrayList<>();

    List<Districts> getDistrictsList() {
        return districtsList;
    }

    class Districts {
        @SerializedName("id")
        @Expose
        private int id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("normalized_name")
        @Expose
        private String normalizedName;
        @SerializedName("assembly_constituencies")
        @Expose
        private List<Constituency> constituencyList = new ArrayList<>();

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<Constituency> getConstituencyList() {
            return constituencyList;
        }

        public String getNormalizedName() {
            return normalizedName;
        }
    }

    class Constituency {
        @SerializedName("id")
        @Expose
        private int id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("normalized_name")
        @Expose
        private String normalizedName;
        @SerializedName("blocks")
        @Expose
        private List<Blocks> blocksList = new ArrayList<>();

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<Blocks> getBlocksList() {
            return blocksList;
        }

        public String getNormalizedName() {
            return normalizedName;
        }
    }

    class Blocks {
        @SerializedName("id")
        @Expose
        private int id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("normalized_name")
        @Expose
        private String normalizedName;
        @SerializedName("grampanchayats")
        @Expose
        private List<Grampanchayat> grampanchayatList = new ArrayList<>();

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getNormalizedName() {
            return normalizedName;
        }

        public List<Grampanchayat> getGrampanchayatList() {
            return grampanchayatList;
        }
    }


    class Grampanchayat {
        @SerializedName("id")
        @Expose
        private int id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("normalized_name")
        @Expose
        private String normalizedName;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getNormalizedName() {
            return normalizedName;
        }
    }
}
