package pl.gk;

public class Mat4 {
    public static float[] identity() {
        return new float[] {
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        };
    }

    public static float[] lookAt(float eyeX, float eyeY, float eyeZ,
                                 float centerX, float centerY, float centerZ,
                                 float upX, float upY, float upZ) {
        // Calculate the forward vector (f)
        float fx = centerX - eyeX;
        float fy = centerY - eyeY;
        float fz = centerZ - eyeZ;

        // Normalize f
        float fLength = (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
        fx /= fLength;
        fy /= fLength;
        fz /= fLength;

        // Calculate the right vector (s)
        float sx = fy * upZ - fz * upY;
        float sy = fz * upX - fx * upZ;
        float sz = fx * upY - fy * upX;

        // Normalize s
        float sLength = (float) Math.sqrt(sx * sx + sy * sy + sz * sz);
        sx /= sLength;
        sy /= sLength;
        sz /= sLength;

        // Calculate the new up vector (u)
        float ux = sz * fy - sy * fz;
        float uy = sx * fz - sz * fx;
        float uz = sy * fx - sx * fy;

        // Create the lookAt matrix
        float[] result = {
                sx, ux, -fx, 0.0f,
                sy, uy, -fy, 0.0f,
                sz, uz, -fz, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        };

        // Translate the view matrix
        translate(result, -eyeX, -eyeY, -eyeZ);

        return result;
    }

    public static float[] perspective(float fov, float aspect, float near, float far) {
        float tanHalfFov = (float) Math.tan(Math.toRadians(fov / 2));
        float[] result = new float[16];

        result[0] = 1.0f / (aspect * tanHalfFov);
        result[1] = 0.0f;
        result[2] = 0.0f;
        result[3] = 0.0f;

        result[4] = 0.0f;
        result[5] = 1.0f / tanHalfFov;
        result[6] = 0.0f;
        result[7] = 0.0f;

        result[8] = 0.0f;
        result[9] = 0.0f;
        result[10] = -(far + near) / (far - near);
        result[11] = -1.0f;

        result[12] = 0.0f;
        result[13] = 0.0f;
        result[14] = -(2.0f * far * near) / (far - near);
        result[15] = 0.0f;

        return result;
    }

    public static void translate(float[] matrix, float x, float y, float z) {
        float tx = matrix[0] * x + matrix[4] * y + matrix[8] * z + matrix[12];
        float ty = matrix[1] * x + matrix[5] * y + matrix[9] * z + matrix[13];
        float tz = matrix[2] * x + matrix[6] * y + matrix[10] * z + matrix[14];
        float tw = matrix[3] * x + matrix[7] * y + matrix[11] * z + matrix[15];

        matrix[12] = tx;
        matrix[13] = ty;
        matrix[14] = tz;
        matrix[15] = tw;
    }
}