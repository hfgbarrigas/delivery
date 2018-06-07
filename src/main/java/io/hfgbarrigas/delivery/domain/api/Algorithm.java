package io.hfgbarrigas.delivery.domain.api;

public enum Algorithm {
    SHORTEST("shortestPath"),
    ALL_SHORTEST("allShortestPaths"),
    ALL("");

    private String alg;

    Algorithm(String alg) {
        this.alg = alg;
    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    @Override
    public String toString() {
        return "Algorithm{" +
                "alg='" + alg + '\'' +
                '}';
    }
}
