package org.neat4j.neat.data.normaliser;

import org.neat4j.neat.data.core.DataKeeper;

import java.util.ArrayList;
import java.util.List;

public class WhitingScaler implements DataScaler  {

    int inputs;
    int outputs;

    public WhitingScaler(int inputs, int outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    @Override
    public DataKeeper normalise(List<List<Double>> dataToNormalize, double minRange, double maxRange) {

        double sredX = 0;
        double sredY = 0;
        double sumX = 0;
        double sumY = 0;
        double sum = 0;
        int i = 1;
        int n = dataToNormalize.size()-1;

        double[] massX = new double [dataToNormalize.get(0).size()-1];
        double[] massY = new double[dataToNormalize.get(0).size() - 1];
        double[] mul = new double[dataToNormalize.get(0).size() - 1];
        double[] cov = new double[n];
        int alfa = 0;
        //среднее значение

        Double[][] array = dataToNormalize.stream().map(doubles -> { return doubles.stream().toArray(Double[]::new);}).toArray(Double[][]::new);

        /*for (int k = 0 ; k < outputs; k++) {
            for (int j = 0; j < array[]; j++)
            {
                alfa = j + 1;
                sumY += Math.Pow(Convert.ToDouble(dataGridView1.Rows[j].Cells[n].Value), alfa);
            }
        }
        sredY = sumY / (dataGridView1.RowCount - 1);
        alfa = 0;
        while (i != dataGridView1.ColumnCount-1)
        {
            sumX = 0;
            sredX = 0;
            sum = 0;
            alfa = 0;
            //сумма по столбцу
            for (int j = 0; j < dataGridView1.RowCount - 1; j++)
            {
                alfa = j+1;
                sumX += Math.Pow(Convert.ToDouble(dataGridView1.Rows[j].Cells[i].Value),alfa);
            }
            //среднее значение
            sredX = sumX / (dataGridView1.RowCount - 1);
            alfa = 0;
            for (int j = 0; j < dataGridView1.RowCount - 1; j++)
            {
                alfa = j + 1;
                massX[j]=Math.Pow((Convert.ToDouble(dataGridView1.Rows[j].Cells[i].Value)),alfa) - sredX;

                massY[j]=Math.Pow((Convert.ToDouble(dataGridView1.Rows[j].Cells[n].Value)),alfa) - sredY;
                mul[j] = massX[j] * massY[j];
            }
            for (int j = 0; j < dataGridView1.RowCount - 1; j++)
            {
                sum += mul[j];
            }
            cov[i] = sum/(dataGridView1.RowCount - 1);
            for (int j = 0; j < dataGridView1.RowCount - 1; j++)
                dataGridView1.Rows[j].Cells[i].Value=(((Convert.ToDouble(dataGridView1.Rows[j].Cells[i].Value))-sredX)*cov[i])/(Math.Sqrt(cov[i]));
            i++;
        }*/
        return null;

    }

    @Override
    public DataKeeper denormalise(List<List<Double>> dataToNormalize) {
        return null;
    }


}
