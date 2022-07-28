package ru.volnamarket.jsonjack;

class SizeType 
{

  int attributeId;
  
  int height;
  int width;
  int length;
  int depth; // it can be alternative for specifying width or for length
  // int[] sizeData; // 

  SizeType(int attributeId){
    this.attributeId = attributeId;
    try{
      switch(attributeId){
        // its Размер attribute
        case 40: 
          // in this case we can have 2 values
          this.width = 1;
          this.length = 1;
          this.height = 0;
          this.depth = 0;
          break;
        case 11: 
          // in this height (Высота)
          this.width = 0;
          this.length = 0;
          this.height = 1;
          this.depth = 0;
          break;
        case 12: 
          // in this width (Ширина)
          this.width = 1;
          this.length = 0;
          this.height = 0;
          this.depth = 0;
          break;
        case 125: 
          // in this depth (Глубина)
          this.width = 0;
          this.length = 1; // seting this oc_product.legth field
          this.height = 0;
          this.depth = 0;
          break;
        case 217: 
          // in this length (Длина)
          this.width = 0;
          this.length = 1;
          this.height = 0;
          this.depth = 0;
          break;
        default:
        // we havnt data about field target
          this.width = 0;
          this.length = 0;
          this.height = 0;
          this.depth = 0;
      }
    } catch (Exception e){
      System.out.println(e.toString());
    }
  }

  /*
   * This method return set oa fields, can be modified
   */
  public String getFields(String[] avars){
    return "height";
  }

}
