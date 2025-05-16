package pl.gk;

public class SphereGenerator {
    private final float radius;
    private final int latitudeCount;
    private final int longitudeCount;
    private float[] vertices;
    private int[] indices;

    public SphereGenerator(float radius, int latitudeCount, int longitudeCount) {
        this.radius = radius;
        this.latitudeCount = latitudeCount;
        this.longitudeCount = longitudeCount;
        generateVerticesAndIndices();
    }

    private void generateVerticesAndIndices() {
        // Total number of vertices = (latitudeCount + 1) * (longitudeCount + 1)
        // Each vertex has 6 floats: 3 for position and 3 for normal
        int vertexCount = (latitudeCount + 1) * (longitudeCount + 1);
        vertices = new float[vertexCount * 6];

        // Generate vertices
        for (int lat = 0; lat <= latitudeCount; lat++) {
            float theta = lat * (float) Math.PI / latitudeCount;
            float sinTheta = (float) Math.sin(theta);
            float cosTheta = (float) Math.cos(theta);

            for (int lon = 0; lon <= longitudeCount; lon++) {
                float phi = lon * 2.0f * (float) Math.PI / longitudeCount;
                float sinPhi = (float) Math.sin(phi);
                float cosPhi = (float) Math.cos(phi);

                float x = cosPhi * sinTheta;
                float y = cosTheta;
                float z = sinPhi * sinTheta;

                // Position (x, y, z)
                int index = (lat * (longitudeCount + 1) + lon) * 6;
                vertices[index] = radius * x;
                vertices[index + 1] = radius * y;
                vertices[index + 2] = radius * z;

                // Normal (x, y, z) - for a sphere, normals are just the normalized position
                vertices[index + 3] = x;
                vertices[index + 4] = y;
                vertices[index + 5] = z;
            }
        }

        // Generate indices for triangles
        // Each quad has 2 triangles, each triangle has 3 indices
        int numQuads = latitudeCount * longitudeCount;
        indices = new int[numQuads * 6];
        int index = 0;

        for (int lat = 0; lat < latitudeCount; lat++) {
            for (int lon = 0; lon < longitudeCount; lon++) {
                int current = lat * (longitudeCount + 1) + lon;
                int next = current + longitudeCount + 1;

                // First triangle
                indices[index++] = current;
                indices[index++] = next;
                indices[index++] = current + 1;

                // Second triangle
                indices[index++] = current + 1;
                indices[index++] = next;
                indices[index++] = next + 1;
            }
        }
    }

    public float[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }

    public int getIndexCount() {
        return indices.length;
    }
}
