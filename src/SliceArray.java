import java.util.ArrayList;


public class SliceArray {
	
	public static int solution(int A[]){
		int curridx = 0;
		int eleCnt = 0;
		
		ArrayList<String> arri = new ArrayList<String>();
		//arri.add(curridx+","+eleCnt);

		for(int i=0; i<A.length-1; i++){
			if(A[i] < A[i+1] & i+1 != A.length-1){
				eleCnt++;
			}else if(A[i] > A[i+1]){
				//System.out.println(i+" "+(A.length));
				eleCnt++;
				arri.add(curridx+","+eleCnt);
				curridx=i+1;
				eleCnt=0;
			}else if(i+1 == A.length-1){
				arri.add(curridx+","+(eleCnt+2));
			}
			
		}
		
		for(String s : arri){
			System.out.println(s);
		}
		int maxIdx = Integer.parseInt(arri.get(0).split(",")[0]);
		int maxVal = Integer.parseInt(arri.get(0).split(",")[1]);
		for(int i=1; i<arri.size(); i++){
			int currIdx = Integer.parseInt(arri.get(i).split(",")[0]);
			int currVal = Integer.parseInt(arri.get(i).split(",")[1]);
			//System.out.println(val);
			if(currVal > maxVal ){
				//System.out.println(val);
				maxIdx = currIdx;

			}
			
		}
		return maxIdx;
	}
	
	public static void main(String [] args){
		int arr[] = {1,2,3,4,5,6,7,8,9,1,2,1,2,3,4,1,2,3,4,5,6};
		System.out.print(solution(arr));
	}

}
