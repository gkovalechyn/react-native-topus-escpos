import { NativeModules } from 'react-native';

type TopusEscposType = {
  multiply(a: number, b: number): Promise<number>;
};

const { TopusEscpos } = NativeModules;

export default TopusEscpos as TopusEscposType;
